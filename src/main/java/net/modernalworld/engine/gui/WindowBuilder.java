package net.modernalworld.engine.gui;

import javax.swing.JFrame;
import java.awt.Dimension;

public class WindowBuilder {
	private String name;
	private String title;
	private boolean resizeable;
	private int width, height;

	public WindowBuilder(String name) {
		this.name = name;
	}

	public WindowBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public WindowBuilder setResizeable(boolean resizeable) {
		this.resizeable = resizeable;
		return this;
	}

	public WindowBuilder setHeight(int height) {
		this.height = height;
		return this;
	}

	public WindowBuilder setWidth(int width) {
		this.width = width;
		return this;
	}

	public Window build() {
		Window wind = new Window(name, new Dimension(width, height));

		JFrame frame = new JFrame(title);
		frame.add(wind);
		frame.setResizable(this.resizeable);
		frame.setName(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);

		wind.setFrame(frame);

		return wind;
	}
}
