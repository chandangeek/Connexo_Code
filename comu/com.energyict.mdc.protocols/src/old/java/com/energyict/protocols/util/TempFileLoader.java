/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TempFileLoader {

    public static byte[] loadTempFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
}