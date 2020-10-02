package net.modernalworld.engine.gui.render;

import java.awt.Graphics;

public class GiverGraphics {
	private Graphics graphics;

	public GiverGraphics(Graphics graphics) {
		this.graphics = graphics;
	}

	public GiverGraphics setGraphics(Graphics graphics) {
		this.graphics = graphics;
		return this;
	}

	public Graphics getGraph() {
		return graphics;
	}

	public void drawAnimation(Animation anim, int x, int y, int width, int height) {
		getGraph().drawImage(anim.current(), x, y, width, height, null);

		anim.frame++;
		int maxFrames = (int) (60 / Math.max(anim.speed, 1));
		if (anim.frame > maxFrames) {
			anim.frame = 0;
			anim.next();
		}
	}

	public void drawAnimation(Animation anim, int x, int y) {
		drawAnimation(anim, x, y, anim.current().getWidth(), anim.current().getHeight());
	}
}
