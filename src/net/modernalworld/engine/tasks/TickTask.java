package net.modernalworld.engine.tasks;

import net.modernalworld.engine.Engine;
import net.modernalworld.engine.GameBase;

public class TickTask extends Thread implements Runnable {
	private int currentUpdates = 0;

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
					game.update();
				} catch (Throwable e) {
					System.out.println("Exception in Game.update()");
				}

				delta--;
				fps++;
			}

			if (System.currentTimeMillis() - timer >= 1000) {
				timer = System.currentTimeMillis();
				currentUpdates = fps;
				fps = 0;
			}
		}
	}

	public int getCurrentUpdates() {
		return this.currentUpdates;
	}
}
