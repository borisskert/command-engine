# Command engine

This solution contains a implementation of an Command Engine which is able to execute Commands in the background.

## Engine Modes

The `BlockingQueueEngine` is able to run in two different modes:

* Sequential executing
* Parallel executing

### Sequential executing

In Sequential-Execution mode the engine instance will
 use one single thread to execute all commands from its queue. The consequence of this is the commands will executed in
 the same order like they are added to the engine's queue. 

### Parallel executing

In Parallel-Execution mode the engine instance will use as many threads as provided by the machine. The consequence of
 this is the commands will be executed in a arbitrary order. 

## Usage

### Create and run your engine

```
// creates a sequential executing engine
Engine engine = BlockingQueueEngine.sequentialExecutingEngine();

// creates a sequential executing engine
Engine engine = BlockingQueueEngine.parallelExecutingEngine();
``` 

### Create commands and add to engine

#### "Classical style"

```
// Create custom command type
class MyCommand implements Command {
    @Override
    public void execute() {
        // <your custom code here>
    }
}
```

```
// Create your command instance
Command myCommand = new MyCommand();
```

```
Engine engine = ...

// Add to your engine
engine.add(myCommand);
```

#### Anonymous class

```
Engine engine = ...

engine.add(new Command() {
    @Override
    public void execute() {
        // <your custom code here>
    }
});
```

#### Lambda style

```
Engine engine = ...

engine.add(() -> {
    // <your custom code here>
});
```

### Start the engine 🚀

```
Engine engine = ...

// Start the engine
// Calling this method will block your thread until the shutdown() method will be called!
engine.start();
```

```
// If you dont want the engine to block your thread, create a new Thread and start the engine:
new Thread(() -> {
    engine.start();
}).start();
```

### Shutdown the engine 💣

```
Engine engine = ...

// Shut down your engine
// Calling this method will also block your current thread until
//  all commands waiting in the engine's queue has been
//  executed by the engine
engine.shutdown();
```

## Build and run tests

Build project:
```shell script
$ mvn compile
```

Run tests:
```shell script
$ mvn test
```

## FAQ

### Why?

Commands are easy to use (like Java's `Runnable`). It's an easy to use solution with not too much code you can use in nearly every Java project.
Also it's not complicated to port it into similar languages like C#.

### May I use, copy, fork or modify code from this project?

Yes, of course! Feel free to use it and leave me a feedback.

### Where can I find the artifact to use this code as maven or gradle dependency?

There is no maven deployment (yet). I suggest you to create your own or just to copy the three files into your project.

## License

* [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
