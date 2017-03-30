/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author Tom De Greyt (tgr)
 */
public class BufferedReaderIterableTest {
    @Test
    public void testIterator() throws Exception {
        String data = "A\nB\nC\nD";
        String[] expected = new String[] {"A", "B", "C", "D"};
        BufferedReader reader = new BufferedReader(new StringReader(data));
        int i = 0;
        for (String line : new BufferedReaderIterable(reader)) {
            assertEquals("Line with index " + i + " is not the expected line.", expected[i], line);
            i++;
        }
    }

    @Test
    public void testIteratorOne() throws Exception {
        String data = "A";
        String[] expected = new String[] {"A"};
        BufferedReader reader = new BufferedReader(new StringReader(data));
        int i = 0;
        for (String line : new BufferedReaderIterable(reader)) {
            assertEquals("Line with index " + i + " is not the expected line.", expected[i], line);
            i++;
        }
    }

    @Test
    public void testIteratorEmpty() throws Exception {
        String data = "";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        for (String line : new BufferedReaderIterable(reader)) {
            fail();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorDoesNotSupportRemove() throws Exception {
        String data = "A\nB\nC\nD";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();
        iterator.next();
        iterator.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorDoesNotGoBeyondEnd() throws Exception {
        String data = "A\nB\nC\nD";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
    }

    @Test
    public void testIteratorWrapsIOException() throws Exception {
        BufferedReader reader = new BufferedReader(new Reader() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }
        });
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();
        try {
            iterator.next();
            fail();
        }
        catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }


}
