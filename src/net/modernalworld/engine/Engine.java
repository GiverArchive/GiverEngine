package net.modernalworld.engine;

import net.modernalworld.engine.tasks.RenderTask;
import net.modernalworld.engine.tasks.TickTask;

public final class Engine
{
  private static Engine instance;

  private GameBase game;
  private TickTask tickTask;
  private RenderTask renderTask;

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

  public GameBase getGame()
  {
    return game;
  }

  public void loadGame(GameBase game)
  {
    if(this.game != null)
      throw  new IllegalStateException("Game already launched.");

    if(game == null)
      throw new IllegalArgumentException("Game cannot be null.");

    this.game = game;
    this.game.start();

    this.tickTask = new TickTask();
    this.tickTask.start();

    this.renderTask = new RenderTask();
    this.renderTask.start();
  }

  public void unloadGame()
  {
    if(game != null)
    {
      game.stop();

      try
      {
        tickTask.join();
        renderTask.join();
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
