/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SimpleTranslationKey implements TranslationKey {

    private String key;
    private String defaultFormat;

    public SimpleTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static List<TranslationKey> loadFromInputStream(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);

        return prop.entrySet().stream().map(e -> new SimpleTranslationKey((String) e.getKey(), (String) e.getValue())).collect(Collectors.toList());
    }
}
