package net.modernalworld.engine.tasks;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.GameBase;

public class RenderTask extends Thread implements Runnable {
	private int currentFPS = 0;

	@Override
	public void run() {
		GameBase game = Engine.getInstance().getGame();

		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		long now;

		double ticks = game.getUpdates();
		double ns = 1000000000 / ticks;
		double delta = 0.0D;

		int fps = 0;

		while (game.isRunning()) {
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;

			if (delta >= 1) {
				try {
					game.render();
				} catch (Throwable t) {
					System.out.println("Exception in Game.render()");
				}

				delta--;
				fps++;
			}

			if (System.currentTimeMillis() - timer >= 1000) {
				timer = System.currentTimeMillis();
				currentFPS = fps;
				fps = 0;
			}
		}
	}

	public int getCurrentFPS() {
		return currentFPS;
	}
}
