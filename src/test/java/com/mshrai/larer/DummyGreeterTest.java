package com.mshrai.larer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Contract tests for the published API: consumers depend on {@link DummyGreeter#greet(String)}
 * behaving as asserted here.
 */
class DummyGreeterTest {

    @Test
    void greetUsesNameWhenProvided() {
        assertEquals("Hello, team!", DummyGreeter.greet("team"));
    }

    @Test
    void greetTrimsWhitespace() {
        assertEquals("Hello, team!", DummyGreeter.greet("  team  "));
    }

    @Test
    void greetUsesWorldWhenNull() {
        assertEquals("Hello, world!", DummyGreeter.greet(null));
    }

    @Test
    void greetUsesWorldWhenBlank() {
        assertEquals("Hello, world!", DummyGreeter.greet(""));
        assertEquals("Hello, world!", DummyGreeter.greet("   "));
    }

    @Test
    void classIsNotInstantiable() {
        var ex =
                assertThrows(
                        InvocationTargetException.class,
                        () -> {
                            var c = DummyGreeter.class.getDeclaredConstructor();
                            c.setAccessible(true);
                            c.newInstance();
                        });
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }
}
