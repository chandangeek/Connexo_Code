package com.energyict.protocols.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/05/2016 - 15:38
 */
public class TempFileLoader {

    public static byte[] loadTempFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
}