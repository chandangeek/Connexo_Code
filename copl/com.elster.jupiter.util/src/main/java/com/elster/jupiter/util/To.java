package com.elster.jupiter.util;

import com.google.common.base.Function;

import java.io.File;
import java.nio.file.Path;

public enum To {
    ;

    public static Function<HasName, String> NAME = new Function<HasName, String>() {
        @Override
        public String apply(HasName hasName) {
            return hasName == null ? null : hasName.getName();
        }
    };

    public static Function<Path, File> FILE = new Function<Path, File>() {
        @Override
        public File apply(Path path) {
            return path == null ? null : path.toFile();
        }
    };
}
