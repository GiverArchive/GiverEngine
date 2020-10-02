package net.modernalworld.engine.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.List;
import net.modernalworld.engine.game.GameBase;

// BukkitScheduler - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/scheduler/BukkitScheduler.java
public interface Scheduler
{
  int scheduleSyncDelayedTask(GameBase game, Runnable task, long delay);
  
  @Deprecated
  int scheduleSyncDelayedTask(GameBase game, TaskRunnable task, long delay);
  
  int scheduleSyncDelayedTask(GameBase game, Runnable task);
  
  @Deprecated
  int scheduleSyncDelayedTask(GameBase game, TaskRunnable task);
  
  int scheduleSyncRepeatingTask(GameBase game, Runnable task, long delay, long period);
  
  @Deprecated
  int scheduleSyncRepeatingTask(GameBase game, TaskRunnable task, long delay, long period);
  
  @Deprecated
  int scheduleAsyncDelayedTask(GameBase game, Runnable task, long delay);
  
  @Deprecated
  int scheduleAsyncDelayedTask(GameBase game, Runnable task);
  
  @Deprecated
  int scheduleAsyncRepeatingTask(GameBase game, Runnable task, long delay, long period);
  
  <T> Future<T> callSyncMethod(GameBase game, Callable<T> task);
  
  void cancelTasks(GameBase game);
  
  void cancelTask(int taskID);
  
  void cancelAllTasks();
  
  boolean isCurrentlyRunning(int taskId);
  
  boolean isQueued(int taskId);
  
  List<Worker> getActiveWorkers();
  
  List<Task> getPendingTasks();
  
  Task runTask(GameBase game, Runnable task) throws IllegalArgumentException;
  
  @Deprecated
  Task runTask(GameBase game, TaskRunnable task) throws IllegalArgumentException;
  
  Task runTaskAsynchronously(GameBase game, Runnable task) throws IllegalArgumentException;
  
  @Deprecated
  Task runTaskAsynchronously(GameBase game, TaskRunnable task) throws IllegalArgumentException;
  
  Task runTaskLater(GameBase game, Runnable task, long delay) throws IllegalArgumentException;
  
  @Deprecated
  Task runTaskLater(GameBase game, TaskRunnable task, long delay) throws IllegalArgumentException;
  
  Task runTaskLaterAsynchronously(GameBase game, Runnable task, long delay) throws IllegalArgumentException;
  
  @Deprecated
  Task runTaskLaterAsynchronously(GameBase game, TaskRunnable task, long delay) throws IllegalArgumentException;
  
  Task runTaskTimer(GameBase game, Runnable task, long delay, long period) throws IllegalArgumentException;
  
  @Deprecated
  Task runTaskTimer(GameBase game, TaskRunnable task, long delay, long period) throws IllegalArgumentException;
  
  Task runTaskTimerAsynchronously(GameBase game, Runnable task, long delay, long period) throws IllegalArgumentException;
  
  @Deprecated
  Task runTaskTimerAsynchronously(GameBase game, TaskRunnable task, long delay, long period) throws IllegalArgumentException;
}
