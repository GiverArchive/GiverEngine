package net.modernalworld.engine.game;

import net.modernalworld.engine.scheduler.core.SchedulerImpl;

public class GameLooping implements Runnable
{
  private SchedulerImpl scheduler;
  private GameBase game;
  
  private volatile boolean running;
  private int totalTicks;
  
  private int fps = 0;
  private int ticks = 0;
  
  public GameLooping(GameBase game, SchedulerImpl scheduler)
  {
    this.game = game;
    this.running = true;
    this.scheduler = scheduler;
    
    new Thread(this, "Engine - Game Looping").start();
  }
  
  @Override
  public void run()
  {
    long timer = System.currentTimeMillis();
    long lastTime = System.nanoTime();
    long now;
    
    double unprocessed = 0.0D;
    double nsTick = 1_000_000_000D / game.getMaxUpdates();
    
    int fps = 0;
    int ticks = 0;
    
    while(running)
    {
      now = System.nanoTime();
      unprocessed += (now - lastTime) / nsTick;
      lastTime = now;
      
      while(unprocessed >= 1)
      {
        ++ticks;
        ++totalTicks;
        game.update();
        scheduler.mainThreadHeartbeat(totalTicks);
        --unprocessed;
      }
      
      game.render();
      fps++;
      
      if(System.currentTimeMillis() - timer >= 1000)
      {
        this.ticks = ticks;
        this.fps = fps;
        ticks = 0;
        fps = 0;
        timer += 1000;
      }
  
      // Estabilizar FPS e reduzir uso de CPU
      try
      {
        Thread.sleep(2);
      }
      catch(InterruptedException ignored) { }
    }
  }
  
  public synchronized void handleStop()
  {
    running = false;
  }
}
