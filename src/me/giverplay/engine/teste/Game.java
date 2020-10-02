package me.giverplay.engine.teste;

import net.modernalworld.engine.GameBase;
import net.modernalworld.engine.gui.Window;
import net.modernalworld.engine.gui.WindowBuilder;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public class Game extends GameBase
{
  private Window window;
  private Graphics graphics;
  private BufferStrategy bufferStrategy;

  public Game()
  {
    super("Testelandia", true, 60, 60);
  }
  
  @Override
  public void onEnable()
  {
    setupFrame();
  }
  
  @Override
  public void onDisable()
  {
  
  }
  
  @Override
  public void update()
  {

  }

  @Override
  public void render()
  {

  }
  
  private void setupFrame()
  {
    this.window = new WindowBuilder("Teste")
        .setResizeable(false)
        .setWidth(640)
        .setHeight(460)
        .setTitle("Testelandiaaaa")
        .build();
    
    this.window.createBufferStrategy(3);
    this.bufferStrategy = this.window.getBufferStrategy();
    this.graphics = this.bufferStrategy.getDrawGraphics();
    
    this.window.showWindow();
  }
}
