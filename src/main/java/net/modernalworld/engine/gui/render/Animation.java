package net.modernalworld.engine.gui.render;

import java.awt.image.BufferedImage;

public class Animation {
	public BufferedImage[] images;
	public int index;
	public int frame;
	public float speed;
	
	public Animation(int speed, BufferedImage... images) {
		this.images = images;
	}
	
	public void next() {
		index++;
		if(index == images.length) {
			index = 0;
		}
	}
	
	public void previus() {
		index--;
		if(index == 0) {
			index = images.length;
		}
	}
	
	public BufferedImage current() {
		return images[index];
	}
}
