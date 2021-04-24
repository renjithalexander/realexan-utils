# realexan-utils
Contains utilities that I've written at different points in time, which I thought are worth sharing.

The main features are:
1. OrderedExecutor framework
2. Debounce function framework

## Ordered Executor framework
This is an executor framework which runs tasks with different keys in parallel, and those with identical keys in series, in the same order that they were submitted.

### Use case

In a pub-sub messaging system, if an executor framework with multiple threads is used to deliver events to the subscribers, the messages for the same topic which are sent in rapid succession can reache the subscribers in jumbled order, as different threads in the thread pool deliver the messages. In such a case, the messages need to be ordered with their topic, without losing the parallel processing. Ordered executor framework was written to solve this issue. It runs tasks which have the same keys in series while the tasks with different keys are run in parallel. In this case the key is topic, and the client can submit as they reach.
