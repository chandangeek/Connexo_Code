package com.elster.jupiter.ftpclient.impl;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;

class RegexPathMatcher implements PathMatcher {

    private final Pattern pattern;


    public RegexPathMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(Path path) {
        return pattern.matcher(path.toString()).matches();
    }
}
