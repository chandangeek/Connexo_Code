/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterable wrapper around a BufferedReader, so that you can simply make an advanced for loop over the lines in a file for instance.
 * Note that IOExceptions that occur on the BufferedReader will be wrapped in a RuntimeException
 * (this in order to circumvent the problem that IOException is a checked exception, which the Iterator cannot handle, but also cannot add to its method signature,
 *  as it is fixed by the interface it needs to implement.
 *
 * @author Tom De Greyt (tgr)
 */
public class BufferedReaderIterable implements Iterable<String> {

    public BufferedReaderIterable(BufferedReader reader) {
        this.reader = reader;
    }

    private BufferedReader reader;

    private class BufferedReaderIterator implements Iterator<String> {

        private String nextLine;

        public boolean hasNext() {
            if (nextLine != null) {
                return true;
            }
            readNextLine();
            return nextLine != null;
        }

        private void readNextLine() {
            try {
                nextLine = reader.readLine();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return pop();
        }

        private String pop() {
            String value = nextLine;
            nextLine = null;
            return value;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<String> iterator() {
        return new BufferedReaderIterator();
    }
}
