package net.modernalworld.engine.scheduler;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.GameBase;

public abstract class BukkitRunnable implements Runnable
{
  private int taskId = -1;
  
  public synchronized void cancel() throws IllegalStateException
  {
    Engine.getInstance().getScheduler().cancelTask(getTaskId());
  }
  
  public synchronized CraftTask runTask(GameBase game) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTask(game, (Runnable) this));
  }
  
  public synchronized CraftTask runTaskAsynchronously(GameBase game) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTaskAsynchronously(game, (Runnable) this));
  }
 
  public synchronized CraftTask runTaskLater(GameBase game, long delay) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTaskLater(game, (Runnable) this, delay));
  }
  
  public synchronized CraftTask runTaskLaterAsynchronously(GameBase game, long delay) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTaskLaterAsynchronously(game, (Runnable) this, delay));
  }
  
  public synchronized CraftTask runTaskTimer(GameBase game, long delay, long period) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTaskTimer(game, (Runnable) this, delay, period));
  }
  
  
  public synchronized CraftTask runTaskTimerAsynchronously(GameBase game, long delay, long period) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getInstance().getScheduler().runTaskTimerAsynchronously(game, (Runnable) this, delay, period));
  }
  
  public synchronized int getTaskId() throws IllegalStateException
  {
    final int id = taskId;
    
    if(id == -1)
    {
      throw new IllegalStateException("Not scheduled yet");
    }
    return id;
  }
  
  private void checkState()
  {
    if(taskId != -1)
    {
      throw new IllegalStateException("Already scheduled as " + taskId);
    }
  }
  
  private CraftTask setupId(final CraftTask task)
  {
    this.taskId = task.getTaskId();
    return task;
  }
}
