package net.modernalworld.engine;

import net.modernalworld.engine.tasks.Task;
import static net.modernalworld.engine.GameBase.getGame;

public final class Engine {
	private static Engine instance;

	private GameBase game;
	private Task task;

	private Engine() {
	}

	public static Engine getInstance() {
		if (instance == null) {
			synchronized (Engine.class) {
				if (instance == null) {
					instance = new Engine();
				}
			}
		}

		return instance;
	}

	public GameBase getGame() {
		return game;
	}

	public void loadGame(GameBase game) {
		if (this.game != null)
			throw new IllegalStateException("Game already launched.");

		if (game == null)
			throw new IllegalArgumentException("Game cannot be null.");

		this.game = game;
		this.game.start();

		this.task = new Task();
		
		this.task.add(getGame()::update);
		this.task.add(getGame()::render);
		
		this.task.start();
	}

	public void unloadGame() {
		if (game != null) {
			game.stop();
		}
	}
}
