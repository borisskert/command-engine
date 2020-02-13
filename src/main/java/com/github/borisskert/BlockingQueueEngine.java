package com.github.borisskert;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BlockingQueueEngine implements Engine {

    private final ExecutorService executorService;
    private final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    private final Object synchronization = new Object();

    private State state = State.CREATED;

    private BlockingQueueEngine(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void add(Command command) {
        if (state == State.SHUTTING_DOWN || state == State.HAS_BEEN_SHUT_DOWN) {
            throw new IllegalStateException("You cannot enqueue new commands to this engine cause it has been shut down");
        }

        queueCommand(command);
    }

    @Override
    public void start() {
        if (state == State.RUNNING) {
            throw new IllegalStateException("You cannot start this engine cause it has been started already");
        }

        changeState(State.RUNNING);

        while (state == State.RUNNING || state == State.SHUTTING_DOWN) {
            Command command = takeNextCommand();
            executorService.submit(command::execute);
        }
    }

    @Override
    public void shutdown() {
        if (state != State.RUNNING) {
            throw new IllegalStateException("You cannot shutdown this engine cause it has not been started");
        }

        changeState(State.SHUTTING_DOWN);

        queueCommand(this::release);

        waitForRelease();

        executorService.shutdown();
        changeState(State.HAS_BEEN_SHUT_DOWN);
    }

    private void changeState(State state) {
        this.state = state;
        this.listener.accept(this.state);
    }

    /**
     * https://stackoverflow.com/a/886799
     */
    private void waitForRelease() {
        try {
            synchronized (synchronization) {
                synchronization.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void release() {
        synchronized (synchronization) {
            synchronization.notify();
        }
    }

    private Command takeNextCommand() {
        Command command;

        try {
            command = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return command;
    }

    private void queueCommand(Command command) {
        try {
            queue.put(command);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Engine serialExecutingEngine() {
        return new BlockingQueueEngine(Executors.newSingleThreadExecutor());
    }

    public static Engine parallelExecutingEngine() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        return new BlockingQueueEngine(executorService);
    }

    enum State {
        CREATED,
        RUNNING,
        SHUTTING_DOWN,
        HAS_BEEN_SHUT_DOWN,
    }

    // Only for testing purposes
    private Consumer<State> listener = state -> {};
    void onStateChange(Consumer<State> listener) {
        this.listener = listener;
    }
}
