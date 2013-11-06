package com.elster.jupiter.util.collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.fest.assertions.api.Assertions.assertThat;

public class ArrayStackTest {

    private ArrayStack<String> stack;

    @Before
    public void setUp() {
        stack = new ArrayStack<>();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testPopWhatIsLastPushed() {
        stack.push("A");
        stack.push("B");

        assertThat(stack.pop()).isEqualTo("B");
    }

    @Test(expected = NoSuchElementException.class)
    public void testPopOnEmptyStackThrowsNoSuchElementException() {
        stack.pop();
    }

    @Test
    public void testPeekOnEmptyStackReturnsNull() {
        assertThat(stack.peek()).isNull();
    }

    @Test
    public void testPopReturnsWhatWasPeekedIfStackNotEmpty() {
        stack.push("A");
        stack.push("B");

        assertThat(stack.peek()).isEqualTo(stack.pop());
    }

}
