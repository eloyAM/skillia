package com.example.application.ut.utils;

import com.example.application.utils.FunctionalUtils;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FunctionalUtilsTest {
    @Test
    void streamToIterable() {
        Iterable<Integer> actual = FunctionalUtils.streamToIterable(
                Stream.of(1, 2, 3)
        );
        assertThat(actual).containsExactlyInAnyOrder(1, 2, 3);
        assertThatThrownBy(actual::iterator)
                .isInstanceOf(IllegalStateException.class); // Stream can be iterated only once
    }

    @Test
    void iterableToStream() {
        Stream<Integer> actual = FunctionalUtils.iterableToStream(
                Stream.of(1, 2, 3)::iterator
        );
        assertThat(actual).containsExactlyInAnyOrder(1, 2, 3);
    }
}
