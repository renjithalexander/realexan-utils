package com.realexan.event;

import java.util.Set;

/**
 * The implementation dispatches the event to the set of event listeners
 * provided. The threading strategy of event notification is defined by the
 * specific implementation.
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
public interface EventDispatcher<T> {

    /**
     * Dispatches the event object to the set of event listeners supplied.
     * 
     * @param eventId   the event ID for the notification framework.
     * @param listeners set of listener which are needed to be notified.
     * @param eventData the object containing the information about the event.
     */
    public void submit(String eventId, Set<? extends EventListener<T>> listeners, T eventData);

}
