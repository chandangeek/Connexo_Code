package com.elster.jupiter.util;

import com.google.common.base.Function;

import java.io.File;
import java.nio.file.Path;

/**
 * Starting point for fluent API calls that produce Functions.
 */
public enum To {
    ;

    public static final Function<HasName, String> NAME = new Function<HasName, String>() {
        @Override
        public String apply(HasName hasName) {
            return hasName == null ? null : hasName.getName();
        }
    };

    public static final Function<Path, File> FILE = new Function<Path, File>() {
        @Override
        public File apply(Path path) {
            return path == null ? null : path.toFile();
        }
    };
}
