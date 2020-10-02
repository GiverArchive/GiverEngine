package net.modernalworld.engine.scheduler;

// BukkitWorker - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/scheduler/BukkitWorker.java
public interface Worker
{
  int getTaskId();
  
  Thread getThread();
}
