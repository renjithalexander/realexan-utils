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
    
    // The defined sequence of execution for the shared key executables would be:
    // 1->2 (key a)
    // 3->5->6 (key b)
    // 4->7->8 (key c)
    // 9 (key d)

```
