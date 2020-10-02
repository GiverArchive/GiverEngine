package me.giverplay.engine.teste;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.game.GameBase;
import net.modernalworld.engine.gui.Window;
import net.modernalworld.engine.gui.WindowBuilder;
import net.modernalworld.engine.gui.render.Animation;
import net.modernalworld.engine.gui.render.GiverGraphics;
import net.modernalworld.engine.scheduler.TaskRunnable;

public class Game extends GameBase {
  
  private Window window;
  private BufferStrategy bufferStrategy;
  private GiverGraphics gp;
  
  public Game() {
    super("Testelandia", true, 60, 60);
  }
  
  private void setupFrame() {
    WindowBuilder builder = new WindowBuilder("Teste");
    builder.setResizeable(false);
    builder.setWidth(640);
    builder.setHeight(460);
    builder.setTitle("Testelandiaaaa");
    
    this.window = builder.build();
    
    this.window.createBufferStrategy(3);
    this.bufferStrategy = this.window.getBufferStrategy();
    
    this.gp = new GiverGraphics(this.bufferStrategy.getDrawGraphics());
    
    this.window.showWindow();
  }
  
  @Override
  public void onEnable()
  {
    setupFrame();
    
    new TaskRunnable()
    {
      @Override
      public void run()
      {
        System.out.println("Dentro do scheduler repetindo a cada 1 segundos");
      }
    }.runTaskTimer(this, 10, 60);
    
    new TaskRunnable()
    {
      @Override
      public void run()
      {
        System.out.println("Dentro do scheduler repetindo a cada 5 segundos");
      }
    }.runTaskTimer(this, 10, 60 * 5);
  
  }
  
  @Override
  public void onDisable()
  {
  
  }
  
  @Override
  public void update() {
  
  }
  
  Animation anim = new Animation(1, load("image1.png"), load("image2.png"));
  
  @Override
  public void render() {
    gp = new GiverGraphics(bufferStrategy.getDrawGraphics());
    Graphics g = gp.getGraph();
    
    g.setColor(Color.black);
    g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
    
    gp.drawAnimation(anim, 0, 0, 64, 64);
    
    g.dispose();
    bufferStrategy.show();
  }
  
  public int getFrameWidth() {
    return window.getFrame().getWidth();
  }
  
  public BufferedImage load(String path) {
    try {
      return ImageIO.read(getClass().getResource("/" + path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("ImageNotFound");
    return null;
  }
  
  public int getFrameHeight() {
    return window.getFrame().getHeight();
  }
}
