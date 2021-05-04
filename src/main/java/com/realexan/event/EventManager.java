/**
 * 
 */
package com.realexan.event;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.realexan.thread.NamedThreadFactory;
import com.realexan.util.ConcurrentHashSet;

/**
 * 
 * Generic notification management framework. It can be used by components which
 * requires a listener model. This framework allows registration and
 * de-registration of listeners for events. It also provides default
 * implementations for threading strategies for notification. User defined
 * strategies too are supported.<br>
 * Eventing identifier can be used to name the event manager, such that it would
 * be easy to identify the functionality that uses the eventing framework.
 * 
 * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
 * @version
 *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
 *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
 *          <td width="10%"><b>Date</b></td>
 *          <td width="10%"><b>Author</b></td>
 *          <td width="10%"><b>Version</b></td>
 *          <td width="*"><b>Description</b></td>
 *          </tr>
 *          <tr bgcolor="white" id="TableRowColor">
 *          <td>04-Jan-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class EventManager<T> {
    /**
     * The logger instance.
     */
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    /**
     * Counter used to generate thread IDs.
     */
    private final AtomicInteger threadIDGenerator = new AtomicInteger(1);

    /**
     * The identifier for component or functionality that uses this eventing
     * framework.
     */
    private final String eventingIdentifier;

    /**
     * The list of event listeners.
     */
    private final Set<EventListener<T>> listeners = new ConcurrentHashSet<>();

    /**
     * The event dispatcher.
     */
    private final EventDispatcher<T> dispatcher;

    /**
     * Constructor.
     * 
     * @param eventingIdentifier the identifier for component or functionality that
     *                           uses this eventing framework
     * @param eventDispatcher    the event dispatcher implementation to be used to
     *                           dispatch the events.
     */
    public EventManager(String eventingIdentifier, EventDispatcher<T> eventDispatcher) {
        Objects.requireNonNull(eventingIdentifier);
        Objects.requireNonNull(eventDispatcher);
        this.eventingIdentifier = eventingIdentifier;
        this.dispatcher = eventDispatcher;
        logger.info("[EVENT_MANAGER_CREATE] Created an event manager with id [" + eventingIdentifier
                + "] and dispatcher [" + dispatcher + "].");
    }

    /**
     * Constructor. The default dispatching mechanism will be used, where the event
     * thread itself will be used for relaying the notifications.
     * 
     * @param eventingIdentifier the identifier for component or functionality that
     *                           uses this eventing framework.
     */
    public EventManager(String eventingIdentifier) {
        this(eventingIdentifier, (DispatcherType) null);
    }

    /**
     * Constructor.
     * 
     * @param eventingIdentifier the identifier for component or functionality that
     *                           uses this eventing framework
     * @param dispatcherType     the type of dispatcher which are defined under
     *                           <code>EventManager.DispatcherType</code>.
     */
    public EventManager(String eventingIdentifier, DispatcherType dispatcherType) {
        Objects.requireNonNull(eventingIdentifier);
        this.eventingIdentifier = eventingIdentifier;
        if (dispatcherType == null) {
            dispatcherType = DispatcherType.CALLER_THREAD_DISPATCHER;
        }
        this.dispatcher = createDispatcher(dispatcherType);
        Objects.requireNonNull(this.dispatcher, "Invalid dispatcher type");
        logger.info("[EVENT_MANAGER_CREATE] Created an event manager with id [" + eventingIdentifier
                + "] and dispatcher type [" + dispatcherType + "].");
    }

    /**
     * Returns the eventing identifier.
     * 
     * @return the eventing identifier.
     */
    public String getEventingIdentifier() {
        return eventingIdentifier;
    }

    /**
     * Registers a listener for notifications.
     * 
     * @param listener the event listener.
     * @return true if the listener was registered successfully.
     */
    public boolean registerListener(EventListener<T> listener) {
        if (listener != null) {
            logger.info("[EVENT_MANAGER_REGISTER_LISTENER] Registering a listener [" + listener
                    + "] to the event manager [" + eventingIdentifier + "]");
            return listeners.add(listener);
        }
        return false;
    }

    /**
     * Removes the listener from the notification list, so that no more events will
     * be notified to that listener.
     * 
     * @param listener the event listener.
     * @return true if the listener was removed.
     */
    public boolean removeListener(EventListener<T> listener) {
        if (listener != null) {
            logger.info("[EVENT_MANAGER_REMOVE_LISTENER] Removing the listener [" + listener
                    + "] from the event manager [" + eventingIdentifier + "]");
            return listeners.remove(listener);
        }
        return false;
    }

    /**
     * Removes all the listeners registered.
     */
    public void clearListeners() {
        logger.info("[EVENT_MANAGER_REMOVE_LISTENER] Removing all the listeners from the event manager ["
                + eventingIdentifier + "]");
        listeners.clear();
    }

    /**
     * Notify the listeners with the event object.
     * 
     * @param eventObject the object containing the details of the events.
     */
    public void notifyListeners(T eventObject) {
        logger.info("EVENT_MANAGER_NOTIFY] Event manager [" + eventingIdentifier + "] notifying event [" + eventObject
                + "] to liseners.");
        dispatcher.submit(eventingIdentifier, Collections.unmodifiableSet(listeners), eventObject);
    }

    /**
     * Creates and returns a dispatcher corresponding to the dispatcher type passed.
     * 
     * @param dispatcher the type of the dispatcher.
     * @return a new EventDispatcher instance.
     */
    private EventDispatcher<T> createDispatcher(DispatcherType dispatcher) {
        switch (dispatcher) {
        case CALLER_THREAD_DISPATCHER:
            return callerThreadDispatcher;
        case SINGLE_THREADED_DISPATCHER:
            return new SingleThreadedEventDispatcher();
        case CACHED_THREADPOOL_DISPATCHER:
            return new CachedThreadPoolEventDispatcher();
        case NEW_THREAD_PER_LISTENER_DISPATCHER:
            return newThreadPerListenerDispatcher;
        case NEW_THREAD_PER_NOTIFICATION_DISPATCHER:
            return newThreadPerNotificationDispatcher;
        default:
            break;
        }
        return null;
    }

    /**
     * Dispatcher that dispatches the event using the caller thread.
     */
    private EventDispatcher<T> callerThreadDispatcher = (name, listeners, eventObj) -> listeners.stream()
            .map(listener -> EventListener.toNotificationRunnable(listener, name, eventObj))
            .map(EventManager::toExceptionSuppressedRunnable).forEach(Runnable::run);

    /**
     * Dispatcher which creates and uses a new thread for every listener to be
     * notified.
     */
    private EventDispatcher<T> newThreadPerListenerDispatcher = (name, listeners, eventObj) -> listeners.stream()
            .map(listener -> EventListener.toNotificationRunnable(listener, name, eventObj))
            .map(EventManager::toExceptionSuppressedRunnable)
            .map(r -> new Thread(r, getEventingIdentifier() + "-listenerthread-" + threadIDGenerator.getAndIncrement()))
            .forEach(Thread::start);

    /**
     * Dispatcher which creates and uses a new thread for every event.
     */
    private EventDispatcher<T> newThreadPerNotificationDispatcher = (name, listeners,
            eventObj) -> new Thread(
                    () -> listeners.stream().map(e -> EventListener.toNotificationRunnable(e, name, eventObj))
                            .map(EventManager::toExceptionSuppressedRunnable).forEach(Runnable::run),
                    getEventingIdentifier() + "-notificationthread-" + threadIDGenerator.getAndIncrement()).start();

    /**
     * A dispatcher that uses an executor service to dispatch all the notifications.
     * This can be inherited by all the custom dispatchers that use an executor
     * service to dispatch events.
     * 
     * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
     * @version
     *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
     *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
     *          <td width="10%"><b>Date</b></td>
     *          <td width="10%"><b>Author</b></td>
     *          <td width="10%"><b>Version</b></td>
     *          <td width="*"><b>Description</b></td>
     *          </tr>
     *          <tr bgcolor="white" id="TableRowColor">
     *          <td>05-Jan-2021</td>
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    public static class ThreadPoolExecutorDispatcher<E> implements EventDispatcher<E> {
        /**
         * The executor service to be used for dispatching events.
         */
        private final ExecutorService executor;

        /**
         * Constructor.
         * 
         * @param executor the executor service to be used for dispatching events.
         */
        protected ThreadPoolExecutorDispatcher(ExecutorService executor) {
            Objects.requireNonNull(executor);
            this.executor = executor;
        }

        /**
         * Default implementation for dispatching events with the executor service.
         */
        public void submit(String eventId, Set<? extends EventListener<E>> listeners, E object) {
            listeners.stream().map(listener -> EventListener.toNotificationRunnable(listener, eventId, object))
                    .map(EventManager::toExceptionSuppressedRunnable).forEach(executor::execute);
        };
    }

    /**
     * A ThreadPoolExecutorDispatcher with a single threaded executor used for
     * dispatching events.
     * 
     * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
     * @version
     *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
     *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
     *          <td width="10%"><b>Date</b></td>
     *          <td width="10%"><b>Author</b></td>
     *          <td width="10%"><b>Version</b></td>
     *          <td width="*"><b>Description</b></td>
     *          </tr>
     *          <tr bgcolor="white" id="TableRowColor">
     *          <td>05-Jan-2021</td>
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    private class SingleThreadedEventDispatcher extends ThreadPoolExecutorDispatcher<T> {
        SingleThreadedEventDispatcher() {
            super(Executors.newSingleThreadExecutor(new NamedThreadFactory(getEventingIdentifier() + "-thread")));
        }
    }

    /**
     * A ThreadPoolExecutorDispatcher with a cached thread pool executor service
     * used for dispatching events.
     * 
     * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
     * @version
     *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
     *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
     *          <td width="10%"><b>Date</b></td>
     *          <td width="10%"><b>Author</b></td>
     *          <td width="10%"><b>Version</b></td>
     *          <td width="*"><b>Description</b></td>
     *          </tr>
     *          <tr bgcolor="white" id="TableRowColor">
     *          <td>05-Jan-2021</td>
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    private class CachedThreadPoolEventDispatcher extends ThreadPoolExecutorDispatcher<T> {
        CachedThreadPoolEventDispatcher() {
            super(Executors.newCachedThreadPool(new NamedThreadFactory(getEventingIdentifier() + "-poolthread")));
        }
    }

    /**
     * Encapsulates the <code>Runnable</code> passed inside another
     * <code>Runnable</code> which gracefully handles any exceptions.
     * 
     * @param r the runnable to be encapsulated.
     * @return a <code>Runnable</code> instance that handles the exceptions thrown
     *         during the execution of the composed runnable.
     */
    private static Runnable toExceptionSuppressedRunnable(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (Throwable t) {
                logger.error("[EVENT_MANAGER_NOTIFICATION_ERROR] There was an error while notifying a listener.", t);
            }
        };
    }

    /**
     * Enumeration that defines the types of default dispatchers available.
     * 
     * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
     * @version
     *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
     *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
     *          <td width="10%"><b>Date</b></td>
     *          <td width="10%"><b>Author</b></td>
     *          <td width="10%"><b>Version</b></td>
     *          <td width="*"><b>Description</b></td>
     *          </tr>
     *          <tr bgcolor="white" id="TableRowColor">
     *          <td>04-Jan-2021</td>
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    public static enum DispatcherType {
        /**
         * The dispatcher that dispatches the events in the caller thread itself. Blocks
         * the caller thread until the notifications are all done.
         */
        CALLER_THREAD_DISPATCHER,
        /**
         * The dispatcher that dispatches the events using a single threaded executor
         * service. Doesn't block the caller thread. However, slowness of event
         * processing by the listeners could cascade to the overall event processing
         * throughput. Events are notified sequentially.
         */
        SINGLE_THREADED_DISPATCHER,
        /**
         * The dispatcher that dispatches the events using a cached thread pool executor
         * service. Doesn't block the caller thread, and supports parallel execution of
         * notifications for different events. Idle threads will get destroyed after 60
         * seconds.
         */
        CACHED_THREADPOOL_DISPATCHER,
        /**
         * The dispatcher that creates a new thread and uses it to dispatch a particular
         * event to all the listeners sequentially. Does not block the caller thread.
         * Every event creates a new thread. Sequential notifications for any particular
         * event, while parallel notifications for different events.
         */
        NEW_THREAD_PER_NOTIFICATION_DISPATCHER,
        /**
         * The dispatcher that creates a new thread for notifying each listener for any
         * event. Doesn't block the caller thread. This causes total parallel
         * notifications.
         */
        NEW_THREAD_PER_LISTENER_DISPATCHER;
    }
}
