package net.modernalworld.engine.scheduler.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.modernalworld.engine.game.GameBase;
import net.modernalworld.engine.scheduler.Scheduler;
import net.modernalworld.engine.scheduler.Task;
import net.modernalworld.engine.scheduler.TaskRunnable;
import net.modernalworld.engine.scheduler.Worker;

// CraftScheduler - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/scheduler/CraftScheduler.java
public class SchedulerImpl implements Scheduler
{
  private final AtomicInteger ids = new AtomicInteger(1);
  
  private volatile TaskImpl head = new TaskImpl();
  
  private final AtomicReference<TaskImpl> tail = new AtomicReference<>(head);
  
  private final PriorityQueue<TaskImpl> pending = new PriorityQueue<>(10,
      (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));
  
  private final List<TaskImpl> temp = new ArrayList<>();
  
  private final ConcurrentHashMap<Integer, TaskImpl> runners = new ConcurrentHashMap<>();
  private final Executor executor = Executors.newCachedThreadPool(doBuild());
  private volatile int currentTick = -1;
  
  private AsyncDebugger debugHead = new AsyncDebugger(-1, null, null)
  {
    @Override
    StringBuilder debugTo(StringBuilder string) {return string;}
  };
  
  private AsyncDebugger debugTail = debugHead;
  private static final int RECENT_TICKS;
  
  static
  {
    RECENT_TICKS = 30;
  }
  
  @Override
  public int scheduleSyncDelayedTask(final GameBase game, final Runnable task)
  {
    return this.scheduleSyncDelayedTask(game, task, 0L);
  }
  
  @Override
  public Task runTask(GameBase game, Runnable runnable)
  {
    return runTaskLater(game, runnable, 0L);
  }
  
  @Deprecated
  @Override
  public int scheduleAsyncDelayedTask(final GameBase game, final Runnable task)
  {
    return this.scheduleAsyncDelayedTask(game, task, 0L);
  }
  
  @Override
  public Task runTaskAsynchronously(GameBase game, Runnable runnable)
  {
    return runTaskLaterAsynchronously(game, runnable, 0L);
  }
  
  @Override
  public int scheduleSyncDelayedTask(final GameBase game, final Runnable task, final long delay)
  {
    return this.scheduleSyncRepeatingTask(game, task, delay, -1L);
  }
  
  @Override
  public Task runTaskLater(GameBase game, Runnable runnable, long delay)
  {
    return runTaskTimer(game, runnable, delay, -1L);
  }
  
  @Deprecated
  @Override
  public int scheduleAsyncDelayedTask(final GameBase game, final Runnable task, final long delay)
  {
    return this.scheduleAsyncRepeatingTask(game, task, delay, -1L);
  }
  
  @Override
  public Task runTaskLaterAsynchronously(GameBase game, Runnable runnable, long delay)
  {
    return runTaskTimerAsynchronously(game, runnable, delay, -1L);
  }
  
  @Override
  public int scheduleSyncRepeatingTask(final GameBase game, final Runnable runnable, long delay, long period)
  {
    return runTaskTimer(game, runnable, delay, period).getTaskId();
  }
  
  @Override
  public Task runTaskTimer(GameBase game, Runnable runnable, long delay, long period)
  {
    validate(game, runnable);
    if(delay < 0L)
    {
      delay = 0;
    }
    
    if(period == 0L)
    {
      period = 1L;
    }
    else if(period < -1L)
    {
      period = -1L;
    }
    
    return handle(new TaskImpl(game, runnable, nextId(), period), delay);
  }
  
  @Deprecated
  @Override
  public int scheduleAsyncRepeatingTask(final GameBase game, final Runnable runnable, long delay, long period)
  {
    return runTaskTimerAsynchronously(game, runnable, delay, period).getTaskId();
  }
  
  @Override
  public Task runTaskTimerAsynchronously(GameBase game, Runnable runnable, long delay, long period)
  {
    validate(game, runnable);
    
    if(delay < 0L)
    {
      delay = 0;
    }
    
    if(period == 0L)
    {
      period = 1L;
    }
    else if(period < -1L)
    {
      period = -1L;
    }
    
    return handle(new AsyncTaskImpl(runners, game, runnable, nextId(), period), delay);
  }
  
  @Override
  public <T> java.util.concurrent.Future<T> callSyncMethod(final GameBase game, final Callable<T> task)
  {
    validate(game, task);
    final Future<T> future = new Future<>(task, game, nextId());
    handle(future, 0L);
    return future;
  }
  
  public void cancelTask(final int taskId)
  {
    if(taskId <= 0)
    {
      return;
    }
    
    TaskImpl task = runners.get(taskId);
    
    if(task != null)
    {
      task.cancel0();
    }
    
    task = new TaskImpl(
        new Runnable()
        {
          @Override
          public void run()
          {
            if(!check(SchedulerImpl.this.temp))
            {
              check(SchedulerImpl.this.pending);
            }
          }
          
          private boolean check(final Iterable<TaskImpl> collection)
          {
            final Iterator<TaskImpl> tasks = collection.iterator();
            
            while(tasks.hasNext())
            {
              final TaskImpl task = tasks.next();
              if(task.getTaskId() == taskId)
              {
                task.cancel0();
                tasks.remove();
                if(task.isSync())
                {
                  runners.remove(taskId);
                }
                return true;
              }
            }
            return false;
          }
        });
    
    handle(task, 0L);
    
    for(TaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
    {
      if(taskPending == task)
      {
        return;
      }
      if(taskPending.getTaskId() == taskId)
      {
        taskPending.cancel0();
      }
    }
  }
  
  @Override
  public void cancelTasks(final GameBase game)
  {
    if(game == null)
    {
      throw new IllegalArgumentException("Plugin cannot be null");
    }
    
    final TaskImpl task = new TaskImpl(
        new Runnable()
        {
          @Override
          public void run()
          {
            check(SchedulerImpl.this.pending);
            check(SchedulerImpl.this.temp);
          }
          
          void check(final Iterable<TaskImpl> collection)
          {
            final Iterator<TaskImpl> tasks = collection.iterator();
            while(tasks.hasNext())
            {
              final TaskImpl task = tasks.next();
              if(task.getOwner().equals(game))
              {
                task.cancel0();
                tasks.remove();
                if(task.isSync())
                {
                  runners.remove(task.getTaskId());
                }
              }
            }
          }
        });
    
    handle(task, 0L);
    
    for(TaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
    {
      if(taskPending == task)
      {
        return;
      }
      if(taskPending.getTaskId() != -1 && taskPending.getOwner().equals(game))
      {
        taskPending.cancel0();
      }
    }
    
    for(TaskImpl runner : runners.values())
    {
      if(runner.getOwner().equals(game))
      {
        runner.cancel0();
      }
    }
  }
  
  @Override
  public void cancelAllTasks()
  {
    final TaskImpl task = new TaskImpl(
        () -> {
          Iterator<TaskImpl> it = SchedulerImpl.this.runners.values().iterator();
          while(it.hasNext())
          {
            TaskImpl task1 = it.next();
            task1.cancel0();
            if(task1.isSync())
            {
              it.remove();
            }
          }
          SchedulerImpl.this.pending.clear();
          SchedulerImpl.this.temp.clear();
        });
    
    handle(task, 0L);
    
    for(TaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
    {
      if(taskPending == task)
      {
        break;
      }
      
      taskPending.cancel0();
    }
    
    for(TaskImpl runner : runners.values())
    {
      runner.cancel0();
    }
  }
  
  @Override
  public boolean isCurrentlyRunning(final int taskId)
  {
    final TaskImpl task = runners.get(taskId);
    
    if(task == null || task.isSync())
    {
      return false;
    }
    final AsyncTaskImpl asyncTask = (AsyncTaskImpl) task;
    synchronized(asyncTask.getWorkers())
    {
      return asyncTask.getWorkers().isEmpty();
    }
  }
  
  @Override
  public boolean isQueued(final int taskId)
  {
    if(taskId <= 0)
    {
      return false;
    }
    for(TaskImpl task = head.getNext(); task != null; task = task.getNext())
    {
      if(task.getTaskId() == taskId)
      {
        return task.getPeriod() >= -1L; // The task will run
      }
    }
    
    TaskImpl task = runners.get(taskId);
    return task != null && task.getPeriod() >= -1L;
  }
  
  @Override
  public List<Worker> getActiveWorkers()
  {
    final ArrayList<Worker> workers = new ArrayList<>();
    
    for(final TaskImpl taskObj : runners.values())
    {
      
      if(taskObj.isSync())
      {
        continue;
      }
      
      final AsyncTaskImpl task = (AsyncTaskImpl) taskObj;
      
      synchronized(task.getWorkers())
      {
        workers.addAll(task.getWorkers());
      }
    }
    
    return workers;
  }
  
  @Override
  public List<Task> getPendingTasks()
  {
    final ArrayList<TaskImpl> truePending = new ArrayList<>();
    for(TaskImpl task = head.getNext(); task != null; task = task.getNext())
    {
      if(task.getTaskId() != -1)
      {
        truePending.add(task);
      }
    }
    
    final ArrayList<Task> pending = new ArrayList<>();
    for(TaskImpl task : runners.values())
    {
      if(task.getPeriod() >= -1L)
      {
        pending.add(task);
      }
    }
    
    for(final TaskImpl task : truePending)
    {
      if(task.getPeriod() >= -1L && !pending.contains(task))
      {
        pending.add(task);
      }
    }
    
    return pending;
  }
  
  public void mainThreadHeartbeat(final int currentTick)
  {
    this.currentTick = currentTick;
    final List<TaskImpl> temp = this.temp;
    parsePending();
    
    while(isReady(currentTick))
    {
      final TaskImpl task = pending.remove();
      
      if(task.getPeriod() < -1L)
      {
        if(task.isSync())
        {
          runners.remove(task.getTaskId(), task);
        }
        parsePending();
        continue;
      }
      
      if(task.isSync())
      {
        try
        {
          task.run();
        }
        catch(final Throwable throwable)
        {
          // TODO
        }
        parsePending();
      }
      else
      {
        debugTail = debugTail.setNext(new AsyncDebugger(currentTick + RECENT_TICKS, task.getOwner(), task.getTaskClass()));
        executor.execute(task);
      }
      
      final long period = task.getPeriod(); // State consistency
      
      if(period > 0)
      {
        task.setNextRun(currentTick + period);
        temp.add(task);
      }
      else if(task.isSync())
      {
        runners.remove(task.getTaskId());
      }
    }
    
    pending.addAll(temp);
    temp.clear();
    debugHead = debugHead.getNextHead(currentTick);
  }
  
  private void addTask(final TaskImpl task)
  {
    final AtomicReference<TaskImpl> tail = this.tail;
    TaskImpl tailTask = tail.get();
    
    while(!tail.compareAndSet(tailTask, task))
    {
      tailTask = tail.get();
    }
    
    tailTask.setNext(task);
  }
  
  private TaskImpl handle(final TaskImpl task, final long delay)
  {
    task.setNextRun(currentTick + delay);
    addTask(task);
    return task;
  }
  
  private static void validate(final GameBase game, final Object task)
  {
    if(game == null || task == null)
    {
      throw new IllegalArgumentException("Objects are null");
    }
  }
  
  private int nextId()
  {
    return ids.incrementAndGet();
  }
  
  private void parsePending()
  {
    TaskImpl head = this.head;
    TaskImpl task = head.getNext();
    TaskImpl lastTask = head;
    
    for(; task != null; task = (lastTask = task).getNext())
    {
      if(task.getTaskId() == -1)
      {
        task.run();
      }
      else if(task.getPeriod() >= -1L)
      {
        pending.add(task);
        runners.put(task.getTaskId(), task);
      }
    }
    
    for(task = head; task != lastTask; task = head)
    {
      head = task.getNext();
      task.setNext(null);
    }
    
    this.head = lastTask;
  }
  
  private boolean isReady(final int currentTick)
  {
    return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
  }
  
  @Override
  public String toString()
  {
    int debugTick = currentTick;
    StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - RECENT_TICKS).append('-').append(debugTick).append('{');
    debugHead.debugTo(string);
    return string.append('}').toString();
  }
  
  @Deprecated
  @Override
  public int scheduleSyncDelayedTask(GameBase game, TaskRunnable task, long delay)
  {
    return scheduleSyncDelayedTask(game, (Runnable) task, delay);
  }
  
  @Deprecated
  @Override
  public int scheduleSyncDelayedTask(GameBase game, TaskRunnable task)
  {
    return scheduleSyncDelayedTask(game, (Runnable) task);
  }
  
  @Deprecated
  @Override
  public int scheduleSyncRepeatingTask(GameBase game, TaskRunnable task, long delay, long period)
  {
    return scheduleSyncRepeatingTask(game, (Runnable) task, delay, period);
  }
  
  @Deprecated
  @Override
  public Task runTask(GameBase game, TaskRunnable task) throws IllegalArgumentException
  {
    return runTask(game, (Runnable) task);
  }
  
  @Deprecated
  @Override
  public Task runTaskAsynchronously(GameBase game, TaskRunnable task) throws IllegalArgumentException
  {
    return runTaskAsynchronously(game, (Runnable) task);
  }
  
  @Deprecated
  @Override
  public Task runTaskLater(GameBase game, TaskRunnable task, long delay) throws IllegalArgumentException
  {
    return runTaskLater(game, (Runnable) task, delay);
  }
  
  @Deprecated
  @Override
  public Task runTaskLaterAsynchronously(GameBase game, TaskRunnable task, long delay) throws IllegalArgumentException
  {
    return runTaskLaterAsynchronously(game, (Runnable) task, delay);
  }
  
  @Deprecated
  @Override
  public Task runTaskTimer(GameBase game, TaskRunnable task, long delay, long period) throws IllegalArgumentException
  {
    return runTaskTimer(game, (Runnable) task, delay, period);
  }
  
  @Deprecated
  @Override
  public Task runTaskTimerAsynchronously(GameBase game, TaskRunnable task, long delay, long period) throws IllegalArgumentException
  {
    return runTaskTimerAsynchronously(game, (Runnable) task, delay, period);
  }
  
  // Google Guava - https://github.com/google/guava
  private static ThreadFactory doBuild()
  {
    final ThreadFactory backingThreadFactory = Executors.defaultThreadFactory();
    final AtomicLong count = new AtomicLong(0);
    
    return runnable -> {
      Thread thread = backingThreadFactory.newThread(runnable);
      thread.setName(String.format(Locale.ROOT, "Craft Scheduler Thread - %1$d", count.getAndIncrement()));
      
      return thread;
    };
  }
}
