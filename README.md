# realexan-utils
Contains utilities that I've written at different points in time, which I thought are worth sharing.

The main features are:
1. OrderedExecutor framework
2. Debounce function framework
3. Functional utilities
4. Thread utilities
5. Miscellaneous utilities

## Ordered Executor framework
This is an executor framework which runs tasks with different keys in parallel, and those with identical keys in series, in the same order that they were submitted.

### Motivation

In a pub-sub messaging system, if an executor framework with multiple threads is used to deliver messages to the subscribers, the messages for the same topic which are sent in rapid succession can reach the subscribers in a jumbled order, as different threads in the thread pool are delivering the messages. In such a case, the messages need to be ordered according the ordering defined by the OrderedRunnable.getOrderingKey(), without losing the parallel processing capability.<br>
Ordered executor framework was written to solve this issue. It runs tasks which have the same keys, in series, while the tasks with different keys are run in parallel. <p><p>In this particular example, the key is the pub-sub topic. To state the obvious, the tasks are additionally expected to have an ordering key, or, in other words, the tasks must implement [OrderedRunnable](./src/main/java/com/realexan/executor/ordered/OrderedRunnable.java).

### Example

```java
    // Sample runnable
    public class TestOrderedRunnable implements OrderedRunnable {

        private String orderingKey;
        private String name;
        
        public TestOrderedRunnable(String name, String orderingKey) {
            this.name = name;
            this.orderingKey = orderingKey;
        }

        @Override
        public void run() {
            System.out.println("Finished executing " + name);
        }

        @Override
        public Object getOrderingKey() {
            return orderingKey;
        }
    };
    
    // Client
    private OrderedExecutor executor = new OrderedExecutor("Test");
    
    
    TestOrderedRunnable tests[] = { new TestOrderedRunnable("1", "a"),
                    new TestOrderedRunnable("2", "a"), new TestOrderedRunnable("3", "b"),
                    new TestOrderedRunnable("4", "c"), new TestOrderedRunnable("5", "b"),
                    new TestOrderedRunnable("6", "b"), new TestOrderedRunnable("7", "c"),
                    new TestOrderedRunnable("8", "c"), new TestOrderedRunnable("9", "d") };
                    
    for(TestOrderedRunnable r: tests) {
    	    executor.submit(r);
    }
    
    // The assured sequence of execution for the shared key executables would be:
    // 1->2 (key a)
    // 3->5->6 (key b)
    // 4->7->8 (key c)
    // 9 (key d)

```

## Debounce Function Framework

A framework that enables debouncing functionality.

### Motivation

Certain triggers were supposed to clear a certain cache, and the cache clearing required spawning of a bash process from the java code. However, at a certain point of time a new trigger was added, which was likely to fire in burst mode- up to 24000 in a batch, within a duration of 5-10 seconds. Had to restrict the spawning of so many cache clearing process in such a short duration.

### Implementation

The Debounce can be created by passing a name, a function that needs to be executed on a trigger(which can throw a Throwable), a cool off period(the minimum duration between two runs of the function), a forced run interval(the maximum duration between two consecutive runs if the triggers don't stop), and a flag with which the caller can configure the trigger to be immediate or delayed, and another flag which configures the debounce to run the function in its own single threaded executor.

The client code can create an instance of the Debounce, passing the function to be executed and the other configurations, and attach it(Debounce.run()) to the trigger for the function call. The debounce will take care of controlling the function call frequency.

When the trigger fires for the first time, the Debounce does the function call(if immediate flag is set), and starts the cool-off period. All triggers during this cool-off period will just extend the cool-off period and no function call will be made. When the cool-off period ends, there will be another function call made by debounce only if there has been triggers during the cool-off period.

In case the triggers don't cease to stop, and the cool-off period gets extended indefinitely, the Debounce will initiate a forced function call every "forced run interval", if it is configured. 

Debounce is written to be resource efficient too. Debounce uses a Timer for the functionality. However, this timer is lazily initialized and has a default idle time of 60 seconds, and thus if no trigger fires for over 60 seconds, the Timer is killed. The timer gets re-created when the next trigger fires. Thus there will not be any overhead of a dormant thread for non-frequently used debounces. Similarly, it doesn't add new schedules to the timer in case of burst mode triggers. At any point of time, the Timer will have at most two timer tasks, one for handling function runs and cool off periods, and another to monitor Timer going idle.

### Example

```java

    private Debounce proxyFunction = Debouncer.create("test-debounce", this::actualFunction, 1000, 10000, true, false);

    private void actualFunction() {
        doTheCostlyOperation();
    }
    
    .....
    /**
     * The caller can safely call this function as frequently as required.
     * The Debounce will take care of actual function calls made.
     */ 
    public void trigger() {
        proxyFunction.run()
    }
    
    
    
```
