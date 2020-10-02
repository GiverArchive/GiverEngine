package net.modernalworld.engine.scheduler.core;

import net.modernalworld.engine.game.GameBase;

// CraftAsyncDebugger - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/scheduler/CraftAsyncDebugger.java
class AsyncDebugger
{
  private AsyncDebugger next = null;
  private final int expiry;
  private final GameBase game;
  private final Class<? extends Runnable> clazz;
  
  AsyncDebugger(final int expiry, final GameBase game, final Class<? extends Runnable> clazz)
  {
    this.expiry = expiry;
    this.game = game;
    this.clazz = clazz;
  }
  
  final AsyncDebugger getNextHead(final int time)
  {
    AsyncDebugger next, current = this;
    
    while(time > current.expiry && (next = current.next) != null)
    {
      current = next;
    }
    return current;
  }
  
  final AsyncDebugger setNext(final AsyncDebugger next)
  {
    return this.next = next;
  }
  
  StringBuilder debugTo(final StringBuilder string)
  {
    for(AsyncDebugger next = this; next != null; next = next.next)
    {
      string.append(next.game.getName()).append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
    }
    return string;
  }
}
