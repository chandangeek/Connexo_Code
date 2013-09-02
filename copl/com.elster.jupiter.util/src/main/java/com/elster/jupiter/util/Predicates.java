package com.elster.jupiter.util;

import com.google.common.base.Predicate;

import java.nio.file.Path;

/**
 * Interface for classes that provide general purpose predicates
 */
public interface Predicates {

    Predicate<Path> onlyFiles();

}

