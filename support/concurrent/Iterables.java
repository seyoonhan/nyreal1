package com.han.startup.support.concurrent;

import java.util.*;

public class Iterables {

    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return new ArrayList<>((Collection<T>) iterable);
        } else {
            List<T> list = new LinkedList<>();
            for (T element : iterable) {
                list.add(element);
            }
            return list;
        }
    }

    public static <T> Set<T> toSet(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            // Return a LinkedHashSet instead of HashSet, to preserve iteration order...
            return new LinkedHashSet<>((Collection<T>) iterable);
        } else {
            Set<T> list = new LinkedHashSet<>();
            for (T element : iterable) {
                list.add(element);
            }
            return list;
        }
    }

    public static String toString(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Iterator<?> i = iterable.iterator(); i.hasNext(); ) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static int count(Iterable<?> iterable) {
        int count = 0;
        //noinspection UnusedDeclaration
        for (Object next : iterable) {
            count++;
        }
        return count;
    }

}
