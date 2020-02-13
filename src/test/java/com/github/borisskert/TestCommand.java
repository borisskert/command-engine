package com.github.borisskert;

public class TestCommand implements Command {
    private boolean hasBeenExecuted = false;
    private int executions = 0;

    @Override
    public void execute() {
        hasBeenExecuted = true;
        executions++;
    }

    public boolean isHasBeenExecuted() {
        return hasBeenExecuted;
    }

    public int getExecutions() {
        return executions;
    }
}
