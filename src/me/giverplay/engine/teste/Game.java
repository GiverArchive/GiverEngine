package me.giverplay.engine.teste;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import net.modernalworld.engine.GameBase;
import net.modernalworld.engine.gui.Window;
import net.modernalworld.engine.gui.WindowBuilder;

public class Game extends GameBase {
	private Window window;
	private Graphics graphics;
	private BufferStrategy bufferStrategy;

	public Game() {
		super("Testelandia", true, 60, 60);

		setupFrame();
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
		this.graphics = this.bufferStrategy.getDrawGraphics();

		this.window.showWindow();
	}

	@Override
	public void update() {
		
	}

	@Override
	public void render() {
		Graphics g = bufferStrategy.getDrawGraphics();
		
		g.setColor(Color.black);
		g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
		
		g.dispose();
		bufferStrategy.show();
	}
	
	public int getFrameWidth() {
		return window.getFrame().getWidth();
	}
	
	public int getFrameHeight() {
		return window.getFrame().getHeight();
	}
}
