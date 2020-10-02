package net.modernalworld.engine.scheduler;

import net.modernalworld.engine.game.GameBase;

// BukkitTask - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/scheduler/BukkitTask.java
public interface Task
{
    int getTaskId();
    
    GameBase getOwner();
    
    boolean isSync();
    
    void cancel();
}
