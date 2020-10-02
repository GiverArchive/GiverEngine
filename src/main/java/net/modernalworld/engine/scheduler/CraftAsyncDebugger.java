package net.modernalworld.engine.scheduler;

import net.modernalworld.engine.GameBase;

class CraftAsyncDebugger {
    private CraftAsyncDebugger next = null;
    private final int expiry;
    private final GameBase game;
    private final Class<? extends Runnable> clazz;

    CraftAsyncDebugger(final int expiry, final GameBase game, final Class<? extends Runnable> clazz) {
        this.expiry = expiry;
        this.game = game;
        this.clazz = clazz;

    }

    final CraftAsyncDebugger getNextHead(final int time) {
        CraftAsyncDebugger next, current = this;
        while (time > current.expiry && (next = current.next) != null) {
            current = next;
        }
        return current;
    }

    final CraftAsyncDebugger setNext(final CraftAsyncDebugger next) {
        return this.next = next;
    }

    StringBuilder debugTo(final StringBuilder string) {
        for (CraftAsyncDebugger next = this; next != null; next = next.next) {
            string.append(next.game.getName()).append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }
        return string;
    }
}
