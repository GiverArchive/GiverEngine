package net.modernalworld.engine;

import net.modernalworld.engine.game.GameBase;
import net.modernalworld.engine.game.GameLooping;
import net.modernalworld.engine.scheduler.Scheduler;
import net.modernalworld.engine.scheduler.core.SchedulerImpl;

public final class Engine
{
  private static final Engine INSTANCE = new Engine();
  
  private static SchedulerImpl scheduler;
  private static GameLooping looping;
  private static GameBase game;
  
  private Engine()
  {
    scheduler = new SchedulerImpl();
  }
  
  public static Scheduler getScheduler()
  {
    return scheduler;
  }
  
  public static GameBase getGame()
  {
    return game;
  }
  
  public static void loadGame(GameBase game)
  {
    if(Engine.game != null)
      throw new IllegalStateException("Game already launched.");
    
    if(game == null)
      throw new IllegalArgumentException("Game cannot be null.");
    
    Engine.game = game;
    Engine.game.onEnable();
    
    Engine.looping = new GameLooping(Engine.game, Engine.scheduler);
  }
  
  public static void shutdown()
  {
    Engine.scheduler.cancelAllTasks();
    Engine.looping.handleStop();
    Engine.game.onDisable();
    System.gc();
    System.exit(0);
  }
}
