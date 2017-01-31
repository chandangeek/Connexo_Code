/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GraphvizInterface {
	
	public byte[] toPng(String source)   {
		return dot(source,"png");
		
	}

    public String tred(String source) {
        return new String(executeProcess("tred", "", source));
    }

    public byte[] toSvg(String source) {
		return dot(source,"svg");
	}

    public byte[] dot(String source, String type) {
        return executeProcess("dot", "-T" + type, source);
    }

    private byte[] executeProcess(String programName, String parameters, String input) {
        ProcessBuilder builder = new ProcessBuilder(programName, parameters);
        try {
            Process process = builder.start();
            try (OutputStream stdIn = process.getOutputStream()) {
                stdIn.write(input.getBytes());
            }
            try (InputStream stdOut = process.getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1 << 14];
                int nRead;
                while ((nRead = stdOut.read(data)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                process.waitFor();
                return buffer.toByteArray();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
