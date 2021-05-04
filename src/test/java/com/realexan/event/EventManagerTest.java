package com.realexan.event;

import static com.realexan.common.ReflectionUtils.getField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.realexan.event.EventManager.DispatcherType;

/**
 * The test class for EventManager.
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
 *          <td>11-Jan-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class EventManagerTest {

    private void cleanup(EventManager<?> eventManager) throws Exception {
        EventDispatcher<?> disp = getField(eventManager, "dispatcher");
        if (disp instanceof EventManager.ThreadPoolExecutorDispatcher<?>) {
            ExecutorService es = getField(disp, "executor");
            es.shutdown();
        }

    }

    private static void expectRuntimeFailure(Runnable r, Class<? extends Throwable> exceptionClass) {
        try {
            r.run();
            fail();
        } catch (Throwable thrown) {
            assertEquals(exceptionClass, thrown.getClass());
        }
    }

    /**
     * Tests the failures while object construction.
     * 
     * @throws Exception
     */
    @Test
    public void testConstructionFailures() throws Exception {
        expectRuntimeFailure(() -> new EventManager<>(null, (EventDispatcher<String>) null),
                NullPointerException.class);
        expectRuntimeFailure(() -> new EventManager<>("testeventmanager", (EventDispatcher<String>) null),
                NullPointerException.class);
        expectRuntimeFailure(() -> new EventManager<>(null, (name, listeners, event) -> {
        }), NullPointerException.class);

        expectRuntimeFailure(() -> new EventManager<>(null), NullPointerException.class);

        expectRuntimeFailure(() -> new EventManager<>(null, (DispatcherType) null), NullPointerException.class);
        expectRuntimeFailure(() -> new EventManager<>(null, DispatcherType.NEW_THREAD_PER_LISTENER_DISPATCHER),
                NullPointerException.class);
    }

    /**
     * Test object construction.
     */
    @Test
    public void testConstruction() throws Exception {
        EventDispatcher<String> testDispatcher = (name, listeners, event) -> {
        };
        EventManager<String> subject = new EventManager<>("testeventmanager", testDispatcher);
        assertEquals(testDispatcher, getField(subject, "dispatcher"));
        assertEquals("testeventmanager", subject.getEventingIdentifier());

        subject = new EventManager<>("testeventmanager2");
        assertEquals("testeventmanager2", subject.getEventingIdentifier());

        subject = new EventManager<>("testeventmanager3", EventManager.DispatcherType.CALLER_THREAD_DISPATCHER);
        assertEquals("testeventmanager3", subject.getEventingIdentifier());
        assertFalse(getField(subject, "dispatcher") instanceof EventManager.ThreadPoolExecutorDispatcher<?>);

        subject = new EventManager<>("testeventmanager4", EventManager.DispatcherType.CACHED_THREADPOOL_DISPATCHER);
        assertEquals("testeventmanager4", subject.getEventingIdentifier());
        assertTrue(getField(subject, "dispatcher") instanceof EventManager.ThreadPoolExecutorDispatcher<?>);
        cleanup(subject);

        subject = new EventManager<>("testeventmanager5", EventManager.DispatcherType.SINGLE_THREADED_DISPATCHER);
        assertEquals("testeventmanager5", subject.getEventingIdentifier());
        assertTrue(getField(subject, "dispatcher") instanceof EventManager.ThreadPoolExecutorDispatcher<?>);
        cleanup(subject);

        subject = new EventManager<>("testeventmanager6",
                EventManager.DispatcherType.NEW_THREAD_PER_LISTENER_DISPATCHER);
        assertEquals("testeventmanager6", subject.getEventingIdentifier());
        assertFalse(getField(subject, "dispatcher") instanceof EventManager.ThreadPoolExecutorDispatcher<?>);

        subject = new EventManager<>("testeventmanager7",
                EventManager.DispatcherType.NEW_THREAD_PER_NOTIFICATION_DISPATCHER);
        assertEquals("testeventmanager7", subject.getEventingIdentifier());
        assertFalse(getField(subject, "dispatcher") instanceof EventManager.ThreadPoolExecutorDispatcher<?>);

    }

    /**
     * Tests register/remove/clear listeners.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddRemoveListener() {
        Object[] args = new Object[3];
        EventDispatcher<String> testDispatcher = (name, listeners, event) -> {
            args[0] = name;
            args[1] = listeners;
            args[2] = event;
        };
        EventListener<String> listener1 = (id, data) -> {
        };
        EventListener<String> listener2 = (id, data) -> {
        };
        EventManager<String> subject = new EventManager<>("testeventmanager", testDispatcher);
        assertTrue(subject.registerListener(listener1));
        subject.notifyListeners("hello");
        assertEquals("testeventmanager", args[0]);
        assertEquals(1, ((Set<EventListener<String>>) args[1]).size());
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener1));
        assertEquals("hello", args[2]);

        assertFalse(subject.registerListener(listener1));
        subject.notifyListeners("hello2");
        assertEquals("testeventmanager", args[0]);
        assertEquals(1, ((Set<EventListener<String>>) args[1]).size());
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener1));
        assertEquals("hello2", args[2]);

        assertTrue(subject.registerListener(listener2));
        subject.notifyListeners("hello3");
        assertEquals("testeventmanager", args[0]);
        assertEquals(2, ((Set<EventListener<String>>) args[1]).size());
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener1));
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener2));
        assertEquals("hello3", args[2]);

        assertTrue(subject.removeListener(listener1));
        subject.notifyListeners("hello4");
        assertEquals("testeventmanager", args[0]);
        assertEquals(1, ((Set<EventListener<String>>) args[1]).size());
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener2));
        assertEquals("hello4", args[2]);

        assertFalse(subject.removeListener(listener1));
        subject.notifyListeners("hello5");
        assertEquals("testeventmanager", args[0]);
        assertEquals(1, ((Set<EventListener<String>>) args[1]).size());
        assertTrue(((Set<EventListener<String>>) args[1]).contains(listener2));
        assertEquals("hello5", args[2]);

        subject.clearListeners();
        subject.notifyListeners("hello6");
        assertEquals("testeventmanager", args[0]);
        assertEquals(0, ((Set<EventListener<String>>) args[1]).size());
        assertEquals("hello6", args[2]);
    }

    /**
     * Tests notifications dispatched via the caller thread.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationCallerThreadDispatcher() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager");
        Thread currentThread = Thread.currentThread();
        subject.registerListener((id, data) -> {
            assertEquals("testeventmanager", id);
            assertEquals(Thread.currentThread(), currentThread);
            assertEquals("hello", data);
        });
        subject.notifyListeners("hello");

        subject = new EventManager<>("testeventmanager", DispatcherType.CALLER_THREAD_DISPATCHER);
        subject.registerListener((id, data) -> {
            assertEquals("testeventmanager", id);
            assertEquals(Thread.currentThread(), currentThread);
            assertEquals("hello2", data);
        });
        subject.notifyListeners("hello2");
    }

    /**
     * Tests notifications dispatched via single threaded pool thread executor.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationSingleThreadedPoolDispatcher() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager",
                DispatcherType.SINGLE_THREADED_DISPATCHER);
        Thread currentThread = Thread.currentThread();
        final CountDownLatch latch = new CountDownLatch(2);
        subject.registerListener((id, data) -> {
            assertEquals("testeventmanager", id);
            assertNotSame(Thread.currentThread(), currentThread);
            assertEquals("testeventmanager-thread-1", Thread.currentThread().getName());
            assertEquals("hello", data);
            latch.countDown();
        });

        subject.registerListener((id, data) -> {
            assertEquals("testeventmanager", id);
            assertNotSame(Thread.currentThread(), currentThread);
            assertEquals("testeventmanager-thread-1", Thread.currentThread().getName());
            assertEquals("hello", data);
            latch.countDown();
        });

        subject.notifyListeners("hello");
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        cleanup(subject);
    }

    /**
     * Tests notifications dispatched via cached thread pool thread executor.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationCachedThreadPoolDispatcher() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager",
                DispatcherType.CACHED_THREADPOOL_DISPATCHER);
        Thread currentThread = Thread.currentThread();
        int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            subject.registerListener((id, data) -> {
                assertEquals("testeventmanager", id);
                assertNotSame(Thread.currentThread(), currentThread);
                assertTrue(Thread.currentThread().getName().startsWith("testeventmanager-poolthread-"));
                assertEquals("hello", data);
                latch.countDown();
            });
        }

        subject.notifyListeners("hello");
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        cleanup(subject);
    }

    /**
     * Tests notifications dispatched via new thread per listener.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationNewThreadPerListenerDispatcher() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager",
                DispatcherType.NEW_THREAD_PER_LISTENER_DISPATCHER);
        Thread currentThread = Thread.currentThread();
        int count = 100;
        int[] notificationThreads = new int[101];
        final CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            subject.registerListener((id, data) -> {
                assertEquals("testeventmanager", id);
                assertNotSame(Thread.currentThread(), currentThread);
                String threadName = Thread.currentThread().getName();
                System.out.println();
                assertTrue(threadName.startsWith("testeventmanager-listenerthread-"));
                assertEquals("hello", data);
                int threadId = Integer.parseInt(threadName.substring("testeventmanager-listenerthread-".length()));
                assertFalse(notificationThreads[threadId] == 1);
                notificationThreads[threadId] = 1;
                latch.countDown();
            });
        }

        subject.notifyListeners("hello");
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        // Verifies that a different thread was used for each listener.
        Arrays.stream(notificationThreads).forEach(Integer.valueOf(1)::equals);
    }

    /**
     * Tests notifications dispatched via new thread per notification.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationNewThreadPerNotificationDispatcher() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager",
                DispatcherType.NEW_THREAD_PER_NOTIFICATION_DISPATCHER);
        Thread currentThread = Thread.currentThread();
        int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            subject.registerListener((id, data) -> {
                assertEquals("testeventmanager", id);
                assertNotSame(Thread.currentThread(), currentThread);
                String threadName = Thread.currentThread().getName();
                assertEquals("testeventmanager-notificationthread-1", threadName);
                assertEquals("hello", data);
                latch.countDown();
            });
        }

        subject.notifyListeners("hello");
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        subject.clearListeners();
        final CountDownLatch latch2 = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            subject.registerListener((id, data) -> {
                assertEquals("testeventmanager", id);
                assertNotSame(Thread.currentThread(), currentThread);
                String threadName = Thread.currentThread().getName();
                assertEquals("testeventmanager-notificationthread-2", threadName);
                assertEquals("hello2", data);
                latch2.countDown();
            });
        }
        subject.notifyListeners("hello2");
        assertTrue(latch2.await(10, TimeUnit.SECONDS));

    }

    /**
     * Tests exception handling while notification.
     * 
     * @throws Exception
     */
    @Test
    public void testExceptionHandling() throws Exception {
        EventManager<String> subject = new EventManager<>("testeventmanager");
        int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            final int identifier = i;
            subject.registerListener((id, data) -> {
                latch.countDown();
                if (identifier % 2 == 0) {
                    throw new RuntimeException();
                }
            });
        }

        subject.notifyListeners("hello");
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
