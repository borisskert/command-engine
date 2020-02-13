package com.github.borisskert;

public class PerformanceCommand implements Command {
    private final int number;

    PerformanceCommand(int number) {
        this.number = number;
    }

    @Override
    public void execute() {
        sleep();
        System.out.println(this.number);
    }

    private void sleep() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
