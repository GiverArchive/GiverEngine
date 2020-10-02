package me.giverplay.engine.teste;

import net.modernalworld.engine.Engine;

public class Main {
	private static Game game;

	public static void main(String[] args) {
		game = new Game();

		Engine.getInstance().loadGame(game);
	}
}
