package net.modernalworld.engine.scheduler.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import net.modernalworld.engine.game.GameBase;
import net.modernalworld.engine.scheduler.Worker;

// CraftAsyncTask - https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/scheduler/CraftAsyncTask.java
class AsyncTaskImpl extends TaskImpl
{

    private final LinkedList<Worker> workers = new LinkedList<Worker>();
    private final Map<Integer, TaskImpl> runners;

    AsyncTaskImpl(final Map<Integer, TaskImpl> runners, final GameBase game, final Runnable task, final int id, final long delay) {
        super(game, task, id, delay);
        this.runners = runners;
    }

    @Override
    public boolean isSync() {
        return false;
    }

    @Override
    public void run() {
        final Thread thread = Thread.currentThread();
        synchronized(workers) {
            if (getPeriod() == -2) {
                // Never continue running after cancelled.
                // Checking this with the lock is important!
                return;
            }
            workers.add(
                new Worker() {
                    public Thread getThread() {
                        return thread;
                    }

                    public int getTaskId() {
                        return AsyncTaskImpl.this.getTaskId();
                    }

                    public GameBase getOwner() {
                        return AsyncTaskImpl.this.getOwner();
                    }
                });
        }
        Throwable thrown = null;
        try {
            super.run();
        } catch (final Throwable t) {
            thrown = t;
            throw new RuntimeException("Exception task " + getTaskId());
        } finally {
            // Cleanup is important for any async task, otherwise ghost tasks are everywhere
            synchronized(workers) {
                try {
                    final Iterator<Worker> workers = this.workers.iterator();
                    boolean removed = false;
                    while (workers.hasNext()) {
                        if (workers.next().getThread() == thread) {
                            workers.remove();
                            removed = true; // Don't throw exception
                            break;
                        }
                    }
                    if (!removed) {
                        throw new IllegalStateException(
                                String.format(
                                    "Unable to remove worker %s on task %s for %s",
                                    thread.getName(),
                                    getTaskId(),
                                    getOwner().getName()),
                                thrown); // We don't want to lose the original exception, if any
                    }
                } finally {
                    if (getPeriod() < 0 && workers.isEmpty()) {
                        // At this spot, we know we are the final async task being executed!
                        // Because we have the lock, nothing else is running or will run because delay < 0
                        runners.remove(getTaskId());
                    }
                }
            }
        }
    }

    LinkedList<Worker> getWorkers() {
        return workers;
    }

    void cancel0() {
        synchronized (workers) {
            // Synchronizing here prevents race condition for a completing task
            setPeriod(-2l);
            if (workers.isEmpty()) {
                runners.remove(getTaskId());
            }
        }
    }
}
