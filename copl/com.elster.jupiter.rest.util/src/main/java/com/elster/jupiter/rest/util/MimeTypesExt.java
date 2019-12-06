/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.rest.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MimeTypesExt {

    private static final MimeTypesExt INSTANCE = new MimeTypesExt();
    private Map<String, String> extMap = new HashMap<>();

    private MimeTypesExt() {
        this.extMap.put("ttf", "font/ttf");
        this.extMap.put("otf", "font/opentype");
        this.extMap.put("woff", "font/woff");
        this.extMap.put("woff2", "font/woff2");
    }

    // returned null leads to org.apache.felix.http.base.internal.util.MimeTypes.getByFile execution
    public String getByFile(String file){
        Optional<String> fileExtension = getFileExtension(file);
        return fileExtension.map(extMap::get).orElse(null);
    }

    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static MimeTypesExt get() {
        return INSTANCE;
    }
}
