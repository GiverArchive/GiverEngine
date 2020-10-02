package net.modernalworld.engine.scheduler.core;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.game.GameBase;
import net.modernalworld.engine.scheduler.Task;

// CraftTask - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/scheduler/CraftTask.java
public class TaskImpl implements Task, Runnable
{
  private volatile TaskImpl next = null;
  /**
   * -1 means no repeating <br>
   * -2 means cancel <br>
   * -3 means processing for Future <br>
   * -4 means done for Future <br>
   * Never 0 <br>
   * >0 means number of ticks to wait between each execution
   */
  private volatile long period;
  private long nextRun;
  private final Runnable task;
  private final GameBase game;
  private final int id;
  
  TaskImpl()
  {
    this(null, null, -1, -1);
  }
  
  TaskImpl(final Runnable task)
  {
    this(null, task, -1, -1);
  }
  
  TaskImpl(final GameBase game, final Runnable task, final int id, final long period)
  {
    this.game = game;
    this.task = task;
    this.id = id;
    this.period = period;
  }
  
  @Override
  public final int getTaskId()
  {
    return id;
  }
  
  @Override
  public final GameBase getOwner()
  {
    return game;
  }
  
  @Override
  public boolean isSync()
  {
    return true;
  }
  
  @Override
  public void run()
  {
    task.run();
  }
  
  long getPeriod()
  {
    return period;
  }
  
  void setPeriod(long period)
  {
    this.period = period;
  }
  
  long getNextRun()
  {
    return nextRun;
  }
  
  void setNextRun(long nextRun)
  {
    this.nextRun = nextRun;
  }
  
  TaskImpl getNext()
  {
    return next;
  }
  
  void setNext(TaskImpl next)
  {
    this.next = next;
  }
  
  Class<? extends Runnable> getTaskClass()
  {
    return task.getClass();
  }
  
  @Override
  public void cancel()
  {
    Engine.getScheduler().cancelTask(id);
  }
  
  void cancel0()
  {
    setPeriod(-2L);
  }
}
