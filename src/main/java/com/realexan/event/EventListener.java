package com.realexan.event;

/**
 * Generic event listener interface.
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
@FunctionalInterface
public interface EventListener<T> {

    /**
     * Call back method for event occurrence.
     * 
     * @param eventId   the event ID for the notification framework.
     * @param eventData the object containing information about the event.
     */
    void eventOccured(String eventId, T eventData);

    /**
     * Creates a notification runnable which actually does the notification.
     * 
     * @param <T>       type of event object.
     * @param e         the event listener.
     * @param eventId   the eventing id.
     * @param eventData the event data.
     * @return a Runnable whose run method would do the notification.
     */
    static <T> Runnable toNotificationRunnable(EventListener<T> e, String eventId, T eventData) {
        return () -> e.eventOccured(eventId, eventData);
    }

}
