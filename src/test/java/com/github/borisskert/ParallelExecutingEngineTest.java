package com.github.borisskert;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class ParallelExecutingEngineTest {

    private Engine engine;

    @BeforeEach
    public void setup() throws Exception {
        engine = BlockingQueueEngine.parallelExecutingEngine();
    }

    @Test
    public void shouldNotAllowToShutDownWhenNotStarted() throws Exception {
        try {
            engine.shutdown();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), Is.is(IsEqual.equalTo("You cannot shutdown this engine cause it has not been started")));
        }
    }

    @Test
    public void shouldNotAllowToStartEngineWhenAlreadyStarted() throws Exception {
        engine.start();

        try {
            engine.start();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(equalTo("You cannot start this engine cause it has been started already")));
        }
    }

    @Test
    public void shouldNotAllowToAddCommandDuringShutDown() throws Exception {
        engine.start();
        engine.shutdown();

        try {
            engine.add(() -> {
                // nothing to do
            });
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(equalTo("You cannot enqueue new commands to this engine cause it has been shut down")));
        }
    }

    @Test
    public void shouldRunCommandBeforeStart() throws Exception {
        TestCommand command = new TestCommand();
        engine.add(command);

        engine.start();
        engine.shutdown();

        assertThat(command.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(command.getExecutions(), is(equalTo(1)));
    }

    @Test
    public void shouldRunCommandAfterStart() throws Exception {
        TestCommand command = new TestCommand();

        engine.start();
        engine.add(command);
        engine.shutdown();

        assertThat(command.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(command.getExecutions(), is(equalTo(1)));
    }

    @Test
    public void shouldRunCommandsAddedBeforeStart() throws Exception {
        TestCommand commandOne = new TestCommand();
        TestCommand commandTwo = new TestCommand();
        TestCommand commandThree = new TestCommand();

        engine.add(commandOne);
        engine.add(commandTwo);
        engine.add(commandThree);

        engine.start();
        engine.shutdown();

        assertThat(commandOne.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandOne.getExecutions(), is(equalTo(1)));

        assertThat(commandTwo.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandTwo.getExecutions(), is(equalTo(1)));

        assertThat(commandThree.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandThree.getExecutions(), is(equalTo(1)));
    }

    @Test
    public void shouldRunCommandsAddedAfterStart() throws Exception {
        TestCommand commandOne = new TestCommand();
        TestCommand commandTwo = new TestCommand();
        TestCommand commandThree = new TestCommand();

        engine.start();

        engine.add(commandOne);
        engine.add(commandTwo);
        engine.add(commandThree);

        engine.shutdown();

        assertThat(commandOne.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandOne.getExecutions(), is(equalTo(1)));

        assertThat(commandTwo.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandTwo.getExecutions(), is(equalTo(1)));

        assertThat(commandThree.isHasBeenExecuted(), is(equalTo(true)));
        assertThat(commandThree.getExecutions(), is(equalTo(1)));
    }
}