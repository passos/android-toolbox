package com.ioenv.android.toolbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liujinyu <simon.jinyu.liu@gmail.com>
 */
public class Stream<T> {
    private Collection<T> data;

    public static <T> Stream<T> of(Collection<T> data) {
        return new Stream<>(data);
    }

    public static <T> Stream<T> of(T... data) {
        return new Stream<>(Arrays.asList(data));
    }

    public Stream(Collection<T> data) {
        this.data = data == null ? Collections.emptyList() : data;
    }

    public interface Func<T, R> {
        R apply(T element);
    }

    public interface VoidFunc<T> {
        void apply(T element);
    }

    public interface Predicate<T> {
        boolean test(T element);
    }

    public interface ReducerFunc<T, R> {
        R apply(R value, T element);
    }

    public List<T> asList() {
        return Collections.list(Collections.enumeration(data));
    }

    public Collection<T> asCollection() {
        return data;
    }

    public T[] asArray() {
        return (T[]) data.toArray();
    }

    public Set<T> asSet() {
        return new HashSet<>(data);
    }

    public <K> Map<K, T> asMap(Func<? super T, K> key) {
        Map<K, T> result = new HashMap<>();
        for (T item : data) {
            result.put(key.apply(item), item);
        }
        return result;
    }

    public int size() {
        return data.size();
    }

    public Stream<T> apply(VoidFunc<? super T> func) {
        for (T item : data) {
            func.apply(item);
        }
        return this;
    }

    public <R> Stream<R> map(Func<? super T, ? extends R> mapper) {
        Collection<R> result = new ArrayList<>(data.size());
        for (T item : data) {
            result.add(mapper.apply(item));
        }
        return Stream.of(result);
    }

    public <R> Stream<R> flatMap(Func<? super T, ? extends Stream<? extends R>> mapper) {
        Collection<R> result = new ArrayList<>(data.size());
        for (T item : data) {
            result.addAll(mapper.apply(item).asCollection());
        }
        return Stream.of(result);
    }

    public Stream<T> filter(Predicate<? super T> predicate) {
        Collection<T> result = new ArrayList<>(data.size());
        for (T item : data) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return new Stream<>(result);
    }

    public <R> R reduce(ReducerFunc<T, R> reducer) {
        return reduce(reducer, null);
    }

    public <R> R reduce(ReducerFunc<T, R> reducer, R initializer) {
        R result = initializer;

        for (T item : data) {
            result = reducer.apply(result, item);
        }
        return result;
    }

    public Stream<T> sort() {
        return sort(null);
    }

    public Stream<T> sort(Comparator<? super T> c) {
        List<T> list = new ArrayList<>(data);
        Collections.sort(list, c);
        return Stream.of(list);
    }

    public Stream<T> copy() {
        return new Stream<>(new ArrayList<>(data));
    }

    public Stream<T> synchronize() {
        return new Stream<>(Collections.synchronizedCollection(data));
    }

    public T first() {
        return first(null);
    }

    public T first(T defaultValue) {
        Iterator<T> iterator = data.iterator();
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }

    public Stream<T> merge(Stream<T> another) {
        data.addAll(another.data);
        return this;
    }

    public Stream<T> intersect(Stream<T> another) {
        Set<T> anotherSet = another.asSet();
        return filter(anotherSet::contains);
    }

    public Stream<T> except(Stream<T> another) {
        Set<T> anotherSet = another.asSet();
        return filter(it -> !anotherSet.contains(it));
    }

    public String join(String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (T item : data) {
            sb.append(delimiter);
            sb.append(item.toString());
        }
        return sb.length() > 0 ? sb.substring(delimiter.length()) : "";
    }
}

