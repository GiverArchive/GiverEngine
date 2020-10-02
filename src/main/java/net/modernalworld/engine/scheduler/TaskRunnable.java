package net.modernalworld.engine.scheduler;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.game.GameBase;

// BukkitRunnable - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/scheduler/BukkitRunnable.java
public abstract class TaskRunnable implements Runnable
{
  private int taskId = -1;
  
  public synchronized void cancel() throws IllegalStateException
  {
    Engine.getScheduler().cancelTask(getTaskId());
  }
  
  public synchronized Task runTask(GameBase game) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTask(game, (Runnable) this));
  }
  
  public synchronized Task runTaskAsynchronously(GameBase game) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTaskAsynchronously(game, (Runnable) this));
  }
 
  public synchronized Task runTaskLater(GameBase game, long delay) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTaskLater(game, (Runnable) this, delay));
  }
  
  public synchronized Task runTaskLaterAsynchronously(GameBase game, long delay) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTaskLaterAsynchronously(game, (Runnable) this, delay));
  }
  
  public synchronized Task runTaskTimer(GameBase game, long delay, long period) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTaskTimer(game, (Runnable) this, delay, period));
  }
  
  
  public synchronized Task runTaskTimerAsynchronously(GameBase game, long delay, long period) throws IllegalArgumentException, IllegalStateException
  {
    checkState();
    return setupId(Engine.getScheduler().runTaskTimerAsynchronously(game, (Runnable) this, delay, period));
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
  
  private Task setupId(final Task task)
  {
    this.taskId = task.getTaskId();
    return task;
  }
}
