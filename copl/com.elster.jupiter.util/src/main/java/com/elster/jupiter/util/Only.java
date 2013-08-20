package com.elster.jupiter.util;

import com.google.common.base.Predicate;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starting point for fluent API calls that produce Predicates.
 */
public enum Only {
    ;

    public static Predicate<Path> FILES = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return !Files.isDirectory(input);
        }
    };

}

