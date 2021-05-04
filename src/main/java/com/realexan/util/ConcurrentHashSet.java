package com.realexan.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A concurrent hash set implementation.
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
 *          <td>16-Sep-2019</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class ConcurrentHashSet<E> implements Set<E> {

    private final ConcurrentHashMap<E, E> map = new ConcurrentHashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        Set<E> keys = map.keySet();
        return keys.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Set<E> keys = map.keySet();
        return keys.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, e) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o, o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Set<E> keys = map.keySet();
        return keys.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= map.put(e, e) == null;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<E> entriesToRemove = map.keySet().stream().filter(k -> !c.contains(k)).collect(Collectors.toSet());
        return removeAll(entriesToRemove);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object e : c) {
            changed |= map.remove(e) != null;
        }
        return changed;
    }

    @Override
    public void clear() {
        map.clear();
    }

    public String toString() {
        return map.keySet().toString();
    }
}
