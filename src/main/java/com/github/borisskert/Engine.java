package com.github.borisskert;

/**
 * Represents an engine to execute {@link Command}s
 */
public interface Engine {
    /**
     * Add a {@link Command} to this engine to be executed later
     *
     * @param command the {@link Command} to be executed later by this {@link Engine}
     */
    void add(Command command);

    /**
     * Start this {@link Engine}
     */
    void start();

    /**
     * Shutdown this {@link Engine}
     */
    void shutdown();
}
