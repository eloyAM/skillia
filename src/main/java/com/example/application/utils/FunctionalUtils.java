package com.example.application.utils;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public final class FunctionalUtils {

    /**
     * Generic method to get an Iterable from a Stream.
     *
     * @implSpec Be careful as this returns a one-shot Iterable
     * (just like a stream, it can be iterated only once).
     * <a href="https://errorprone.info/bugpattern/StreamToIterable">Error-prone advice</a>
     */
    public static <T> Iterable<T> streamToIterable(Stream<T> stream) {
        return stream::iterator;
    }

    // Generic method to get a Stream from an Iterable
    public static <T> Stream<T> iterableToStream(Iterable<T> iterable, boolean parallel) {
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static <T> Stream<T> iterableToStream(Iterable<T> iterable) {
        return iterableToStream(iterable, false);
    }
}
