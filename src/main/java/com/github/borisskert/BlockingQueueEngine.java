package com.github.borisskert;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Implements an {@link Engine} working with an {@link BlockingQueue}.
 * Its {@link Engine#start()} and {@link Engine#shutdown()} methods are blocking the current thread.
 */
public class BlockingQueueEngine implements Engine {

    /* *****************************************************************************************************************
     * Readonly fields
     **************************************************************************************************************** */

    private final ExecutorService executorService;
    private final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    private final Object mutex = new Object();

    /* *****************************************************************************************************************
     * Instance modifiable fields
     **************************************************************************************************************** */

    private State state = State.CREATED;

    /* *****************************************************************************************************************
     * Constructor(s)
     **************************************************************************************************************** */

    /**
     * Creates an instance. Prevent public creation.
     *
     * @param executorService the {@link ExecutorService} used to execute the commands
     */
    private BlockingQueueEngine(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /* *****************************************************************************************************************
     * Implementation of Engine
     **************************************************************************************************************** */

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

    /* *****************************************************************************************************************
     * Private methods
     **************************************************************************************************************** */

    private void changeState(State state) {
        this.state = state;
        this.listener.accept(this.state);
    }

    /**
     * https://stackoverflow.com/a/886799
     */
    private void waitForRelease() {
        try {
            synchronized (mutex) {
                mutex.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void release() {
        synchronized (mutex) {
            mutex.notify();
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

    /* *****************************************************************************************************************
     * Factory methods
     **************************************************************************************************************** */

    public static Engine sequentialExecutingEngine() {
        return new BlockingQueueEngine(Executors.newSingleThreadExecutor());
    }

    public static Engine parallelExecutingEngine() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.print("available processors: ");
        System.out.println(availableProcessors);

        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
        return new BlockingQueueEngine(executorService);
    }

    /* *****************************************************************************************************************
     * Inner class(es) and enum(s)
     **************************************************************************************************************** */

    /**
     * Represents the state of a {@link BlockingQueueEngine}
     * Package private because it's used in unit tests
     */
    enum State {
        CREATED,
        RUNNING,
        SHUTTING_DOWN,
        HAS_BEEN_SHUT_DOWN,
    }

    /* *****************************************************************************************************************
     * Code for easier testing
     **************************************************************************************************************** */

    // Only for testing purposes
    private Consumer<State> listener = state -> {
    };

    /**
     * Register a listener for state changes. Every instance only can use one.
     * The listener will be called when the {@link BlockingQueueEngine#state} of this {@link BlockingQueueEngine} changes.
     *
     * @param listener to be registered for state changes of this instance.
     */
    void registerStateChangeListener(Consumer<State> listener) {
        this.listener = listener;
    }
}
