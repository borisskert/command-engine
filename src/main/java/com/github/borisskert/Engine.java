package com.github.borisskert;

public interface Engine {
    void add(Command command);
    void start();
    void shutdown();
}
