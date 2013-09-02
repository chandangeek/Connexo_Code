package com.elster.jupiter.util;

import com.google.common.base.Predicate;

import java.nio.file.Files;
import java.nio.file.Path;

public class Only implements Predicates {

    private static final Predicate<Path> ONLY_FILES = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return !Files.isDirectory(input);

        }
    };

    @Override
    public Predicate<Path> onlyFiles() {
        return ONLY_FILES;
    }
}
