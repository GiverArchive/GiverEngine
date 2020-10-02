package net.modernalworld.engine.gui;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public class Window extends Canvas {
	private JFrame frame;
	private String name;
	private Dimension dimension;

	public Window(String name, Dimension dimension) {
		this.name = name;
		this.dimension = dimension;
		setPreferredSize(dimension);
	}

	protected void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void showWindow() {
		this.frame.setVisible(true);
	}

	public void hideWindow() {
		this.frame.setVisible(false);
	}

	public int getWidth() {
		return (int) this.dimension.getWidth();
	}

	public int getHeight() {
		return (int) this.dimension.getHeight();
	}
}
