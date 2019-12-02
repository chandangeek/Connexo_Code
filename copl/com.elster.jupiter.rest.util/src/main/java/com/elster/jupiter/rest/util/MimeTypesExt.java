/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.rest.util;

import org.apache.felix.http.base.internal.util.MimeTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MimeTypesExt {

    private static final MimeTypesExt INSTANCE = new MimeTypesExt();
    private final Map<String, String> extMap = new HashMap();

    private MimeTypesExt() {
        this.extMap.put("ttf", "font/ttf");
        this.extMap.put("otf", "font/opentype");
        this.extMap.put("woff", "font/woff");
        this.extMap.put("woff2", "font/woff2");
     }

    public String getByFile(String file){
        String value = MimeTypes.get().getByFile(file);
        if (value == null) {
            Optional<String> fileExtension = getFileExtension(file);
            if (fileExtension.isPresent()) {
                value = extMap.get(fileExtension.get());
            }
        }
        return value == null ? "nosniff" : value;
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
