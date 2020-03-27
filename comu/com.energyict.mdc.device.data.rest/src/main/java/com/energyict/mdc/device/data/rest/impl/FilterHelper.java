/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class FilterHelper {

    private FilterHelper() {
    }

    public static Predicate<String> getStringFilterIfAvailable(String name, JsonQueryFilter filter) {
        if (filter.hasProperty(name)) {
            Pattern pattern = getFilterPattern(filter.getString(name));
            if (pattern != null) {
                return s -> pattern.matcher(s == null ? "" : s).matches();
            }
        }
        return s -> true;
    }

    public static Predicate<String> getStringListFilterIfAvailable(String name, JsonQueryFilter filter) {
        if (filter.hasProperty(name)) {
            List<String> entries = filter.getStringList(name);
            List<Pattern> patterns = new ArrayList<>();
            for (String entry : entries) {
                patterns.add(getFilterPattern(entry));
            }
            if (!patterns.isEmpty()) {
                return s -> {
                    boolean match = false;
                    for (Pattern pattern : patterns) {
                        match = match || pattern.matcher(s).matches();
                        if (match) {
                            break;
                        }
                    }
                    return match;
                };
            }
        }
        return s -> true;
    }

    public static Pattern getFilterPattern(String filter) {
        if (filter != null) {
            filter = Pattern.quote(filter.replace('%', '*'));
            return Pattern.compile(filter.replaceAll("([*?])", "\\\\E\\.$1\\\\Q"));
        }
        return null;
    }

}
