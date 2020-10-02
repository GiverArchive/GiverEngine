package net.modernalworld.engine;

import net.modernalworld.engine.scheduler.CraftScheduler;

public final class Engine
{
  private static Engine instance;
  private static CraftScheduler scheduler = new CraftScheduler();
  
  private GameBase game;

  private Engine(){}

  public static Engine getInstance()
  {
    if(instance == null)
    {
      synchronized (Engine.class)
      {
        if(instance == null)
        {
          instance = new Engine();
        }
      }
    }

    return instance;
  }
  
  public CraftScheduler getScheduler()
  {
    return scheduler;
  }
  
  public GameBase getGame()
  {
    return game;
  }
  
}
