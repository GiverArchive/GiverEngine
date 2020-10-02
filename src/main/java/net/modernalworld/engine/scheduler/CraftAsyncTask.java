package net.modernalworld.engine.scheduler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import net.modernalworld.engine.GameBase;

class CraftAsyncTask extends CraftTask {

    private final LinkedList<BukkitWorker> workers = new LinkedList<BukkitWorker>();
    private final Map<Integer, CraftTask> runners;

    CraftAsyncTask(final Map<Integer, CraftTask> runners, final GameBase game, final Runnable task, final int id, final long delay) {
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
                new BukkitWorker() {
                    public Thread getThread() {
                        return thread;
                    }

                    public int getTaskId() {
                        return CraftAsyncTask.this.getTaskId();
                    }

                    public GameBase getOwner() {
                        return CraftAsyncTask.this.getOwner();
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
                    final Iterator<BukkitWorker> workers = this.workers.iterator();
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

    LinkedList<BukkitWorker> getWorkers() {
        return workers;
    }

    boolean cancel0() {
        synchronized (workers) {
            // Synchronizing here prevents race condition for a completing task
            setPeriod(-2l);
            if (workers.isEmpty()) {
                runners.remove(getTaskId());
            }
        }
        return true;
    }
}
