package net.modernalworld.engine.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.modernalworld.engine.GameBase;

public class CraftScheduler
{
  private final AtomicInteger ids = new AtomicInteger(1);
  
  private volatile CraftTask head = new CraftTask();
  
  private final AtomicReference<CraftTask> tail = new AtomicReference<>(head);
  
  private final PriorityQueue<CraftTask> pending = new PriorityQueue<CraftTask>(10,
      (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));
  
  private final List<CraftTask> temp = new ArrayList<CraftTask>();
  
  private final ConcurrentHashMap<Integer, CraftTask> runners = new ConcurrentHashMap<Integer, CraftTask>();
  private volatile int currentTick = -1;
  private final Executor executor = Executors.newCachedThreadPool(doBuild()); // Spigot
  private CraftAsyncDebugger debugHead = new CraftAsyncDebugger(-1, null, null)
  {
    @Override
    StringBuilder debugTo(StringBuilder string) {return string;}
  };
  private CraftAsyncDebugger debugTail = debugHead;
  private static final int RECENT_TICKS;
  
  static
  {
    RECENT_TICKS = 30;
  }
  
  public int scheduleSyncDelayedTask(final GameBase game, final Runnable task)
  {
    return this.scheduleSyncDelayedTask(game, task, 0l);
  }
  
  public CraftTask runTask(GameBase game, Runnable runnable)
  {
    return runTaskLater(game, runnable, 0l);
  }
  
  @Deprecated
  public int scheduleAsyncDelayedTask(final GameBase game, final Runnable task)
  {
    return this.scheduleAsyncDelayedTask(game, task, 0l);
  }
  
  public CraftTask runTaskAsynchronously(GameBase game, Runnable runnable)
  {
    return runTaskLaterAsynchronously(game, runnable, 0l);
  }
  
  public int scheduleSyncDelayedTask(final GameBase game, final Runnable task, final long delay)
  {
    return this.scheduleSyncRepeatingTask(game, task, delay, -1l);
  }
  
  public CraftTask runTaskLater(GameBase game, Runnable runnable, long delay)
  {
    return runTaskTimer(game, runnable, delay, -1l);
  }
  
  @Deprecated
  public int scheduleAsyncDelayedTask(final GameBase game, final Runnable task, final long delay)
  {
    return this.scheduleAsyncRepeatingTask(game, task, delay, -1l);
  }
  
  public CraftTask runTaskLaterAsynchronously(GameBase game, Runnable runnable, long delay)
  {
    return runTaskTimerAsynchronously(game, runnable, delay, -1l);
  }
  
  public int scheduleSyncRepeatingTask(final GameBase game, final Runnable runnable, long delay, long period)
  {
    return runTaskTimer(game, runnable, delay, period).getTaskId();
  }
  
  public CraftTask runTaskTimer(GameBase game, Runnable runnable, long delay, long period)
  {
    validate(game, runnable);
    if(delay < 0l)
    {
      delay = 0;
    }
    if(period == 0l)
    {
      period = 1l;
    } else if(period < -1l)
    {
      period = -1l;
    }
    return handle(new CraftTask(game, runnable, nextId(), period), delay);
  }
  
  @Deprecated
  public int scheduleAsyncRepeatingTask(final GameBase game, final Runnable runnable, long delay, long period)
  {
    return runTaskTimerAsynchronously(game, runnable, delay, period).getTaskId();
  }
  
  public CraftTask runTaskTimerAsynchronously(GameBase game, Runnable runnable, long delay, long period)
  {
    validate(game, runnable);
    if(delay < 0l)
    {
      delay = 0;
    }
    if(period == 0l)
    {
      period = 1l;
    } else if(period < -1l)
    {
      period = -1l;
    }
    return handle(new CraftAsyncTask(runners, game, runnable, nextId(), period), delay);
  }
  
  public <T> Future<T> callSyncMethod(final GameBase game, final Callable<T> task)
  {
    validate(game, task);
    final CraftFuture<T> future = new CraftFuture<T>(task, game, nextId());
    handle(future, 0L);
    return future;
  }
  
  public void cancelTask(final int taskId)
  {
    if(taskId <= 0)
    {
      return;
    }
    CraftTask task = runners.get(taskId);
    if(task != null)
    {
      task.cancel0();
    }
    task = new CraftTask(
        new Runnable()
        {
          public void run()
          {
            if(!check(CraftScheduler.this.temp))
            {
              check(CraftScheduler.this.pending);
            }
          }
          
          private boolean check(final Iterable<CraftTask> collection)
          {
            final Iterator<CraftTask> tasks = collection.iterator();
            while(tasks.hasNext())
            {
              final CraftTask task = tasks.next();
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
    for(CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
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
  
  public void cancelTasks(final GameBase game)
  {
    
    if(game == null)
    {
      throw new IllegalArgumentException("Plugin cannot be null");
    }
    
    final CraftTask task = new CraftTask(
        new Runnable()
        {
          public void run()
          {
            check(CraftScheduler.this.pending);
            check(CraftScheduler.this.temp);
          }
          
          void check(final Iterable<CraftTask> collection)
          {
            final Iterator<CraftTask> tasks = collection.iterator();
            while(tasks.hasNext())
            {
              final CraftTask task = tasks.next();
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
    handle(task, 0l);
    for(CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
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
    for(CraftTask runner : runners.values())
    {
      if(runner.getOwner().equals(game))
      {
        runner.cancel0();
      }
    }
  }
  
  public void cancelAllTasks()
  {
    final CraftTask task = new CraftTask(
        new Runnable()
        {
          public void run()
          {
            Iterator<CraftTask> it = CraftScheduler.this.runners.values().iterator();
            while(it.hasNext())
            {
              CraftTask task = it.next();
              task.cancel0();
              if(task.isSync())
              {
                it.remove();
              }
            }
            CraftScheduler.this.pending.clear();
            CraftScheduler.this.temp.clear();
          }
        });
    handle(task, 0l);
    for(CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext())
    {
      if(taskPending == task)
      {
        break;
      }
      taskPending.cancel0();
    }
    for(CraftTask runner : runners.values())
    {
      runner.cancel0();
    }
  }
  
  public boolean isCurrentlyRunning(final int taskId)
  {
    final CraftTask task = runners.get(taskId);
    if(task == null || task.isSync())
    {
      return false;
    }
    final CraftAsyncTask asyncTask = (CraftAsyncTask) task;
    synchronized(asyncTask.getWorkers())
    {
      return asyncTask.getWorkers().isEmpty();
    }
  }
  
  public boolean isQueued(final int taskId)
  {
    if(taskId <= 0)
    {
      return false;
    }
    for(CraftTask task = head.getNext(); task != null; task = task.getNext())
    {
      if(task.getTaskId() == taskId)
      {
        return task.getPeriod() >= -1l; // The task will run
      }
    }
    CraftTask task = runners.get(taskId);
    return task != null && task.getPeriod() >= -1l;
  }
  
  public List<BukkitWorker> getActiveWorkers()
  {
    final ArrayList<BukkitWorker> workers = new ArrayList<BukkitWorker>();
    for(final CraftTask taskObj : runners.values())
    {
      // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
      if(taskObj.isSync())
      {
        continue;
      }
      final CraftAsyncTask task = (CraftAsyncTask) taskObj;
      synchronized(task.getWorkers())
      {
        // This will never have an issue with stale threads; it's state-safe
        workers.addAll(task.getWorkers());
      }
    }
    return workers;
  }
  
  public List<CraftTask> getPendingTasks()
  {
    final ArrayList<CraftTask> truePending = new ArrayList<CraftTask>();
    for(CraftTask task = head.getNext(); task != null; task = task.getNext())
    {
      if(task.getTaskId() != -1)
      {
        // -1 is special code
        truePending.add(task);
      }
    }
    
    final ArrayList<CraftTask> pending = new ArrayList<CraftTask>();
    for(CraftTask task : runners.values())
    {
      if(task.getPeriod() >= -1l)
      {
        pending.add(task);
      }
    }
    
    for(final CraftTask task : truePending)
    {
      if(task.getPeriod() >= -1l && !pending.contains(task))
      {
        pending.add(task);
      }
    }
    return pending;
  }
  
  /**
   * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
   */
  public void mainThreadHeartbeat(final int currentTick)
  {
    this.currentTick = currentTick;
    final List<CraftTask> temp = this.temp;
    parsePending();
    while(isReady(currentTick))
    {
      final CraftTask task = pending.remove();
      if(task.getPeriod() < -1l)
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
      } else
      {
        debugTail = debugTail.setNext(new CraftAsyncDebugger(currentTick + RECENT_TICKS, task.getOwner(), task.getTaskClass()));
        executor.execute(task);
        // We don't need to parse pending
        // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
      }
      final long period = task.getPeriod(); // State consistency
      if(period > 0)
      {
        task.setNextRun(currentTick + period);
        temp.add(task);
      } else if(task.isSync())
      {
        runners.remove(task.getTaskId());
      }
    }
    pending.addAll(temp);
    temp.clear();
    debugHead = debugHead.getNextHead(currentTick);
  }
  
  private void addTask(final CraftTask task)
  {
    final AtomicReference<CraftTask> tail = this.tail;
    CraftTask tailTask = tail.get();
    while(!tail.compareAndSet(tailTask, task))
    {
      tailTask = tail.get();
    }
    tailTask.setNext(task);
  }
  
  private CraftTask handle(final CraftTask task, final long delay)
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
    CraftTask head = this.head;
    CraftTask task = head.getNext();
    CraftTask lastTask = head;
    for(; task != null; task = (lastTask = task).getNext())
    {
      if(task.getTaskId() == -1)
      {
        task.run();
      } else if(task.getPeriod() >= -1l)
      {
        pending.add(task);
        runners.put(task.getTaskId(), task);
      }
    }
    // We split this because of the way things are ordered for all of the async calls in CraftScheduler
    // (it prevents race-conditions)
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
  public int scheduleSyncDelayedTask(GameBase game, BukkitRunnable task, long delay)
  {
    return scheduleSyncDelayedTask(game, (Runnable) task, delay);
  }
  
  @Deprecated
  public int scheduleSyncDelayedTask(GameBase game, BukkitRunnable task)
  {
    return scheduleSyncDelayedTask(game, (Runnable) task);
  }
  
  @Deprecated
  public int scheduleSyncRepeatingTask(GameBase game, BukkitRunnable task, long delay, long period)
  {
    return scheduleSyncRepeatingTask(game, (Runnable) task, delay, period);
  }
  
  @Deprecated
  public CraftTask runTask(GameBase game, BukkitRunnable task) throws IllegalArgumentException
  {
    return runTask(game, (Runnable) task);
  }
  
  @Deprecated
  public CraftTask runTaskAsynchronously(GameBase game, BukkitRunnable task) throws IllegalArgumentException
  {
    return runTaskAsynchronously(game, (Runnable) task);
  }
  
  @Deprecated
  public CraftTask runTaskLater(GameBase game, BukkitRunnable task, long delay) throws IllegalArgumentException
  {
    return runTaskLater(game, (Runnable) task, delay);
  }
  
  @Deprecated
  public CraftTask runTaskLaterAsynchronously(GameBase game, BukkitRunnable task, long delay) throws IllegalArgumentException
  {
    return runTaskLaterAsynchronously(game, (Runnable) task, delay);
  }
  
  @Deprecated
  public CraftTask runTaskTimer(GameBase game, BukkitRunnable task, long delay, long period) throws IllegalArgumentException
  {
    return runTaskTimer(game, (Runnable) task, delay, period);
  }
  
  @Deprecated
  public CraftTask runTaskTimerAsynchronously(GameBase game, BukkitRunnable task, long delay, long period) throws IllegalArgumentException
  {
    return runTaskTimerAsynchronously(game, (Runnable) task, delay, period);
  }
  
  // Google Guava
  private static ThreadFactory doBuild()
  {
    final ThreadFactory backingThreadFactory = Executors.defaultThreadFactory();
    final AtomicLong count = new AtomicLong(0);
    
    return new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable runnable)
      {
        Thread thread = backingThreadFactory.newThread(runnable);
        thread.setName(format("Craft Scheduler Thread - %1$d", count.getAndIncrement()));
  
        return thread;
      }
    };
  }
  
  private static String format(String format, Object... args)
  {
    return String.format(Locale.ROOT, format, args);
  }
}
