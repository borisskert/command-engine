package com.github.borisskert;

/**
 * Represents a command: its {@link Command#execute()} method will be called by an {@link Engine}
 *
 * @see <a href="https://en.wikipedia.org/wiki/Command_pattern">Read Command pattern in wikipedia</a>
 */
public interface Command {

    /**
     * This method will be called by an {@link Engine}
     */
    void execute();
}
