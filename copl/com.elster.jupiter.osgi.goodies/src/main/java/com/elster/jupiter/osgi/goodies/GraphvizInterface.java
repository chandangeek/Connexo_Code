package com.elster.jupiter.osgi.goodies;

import java.io.*;

public class GraphvizInterface {
	
	public byte[] toPng(String source)   {
		return dot(source,"png");
		
	}
	
	public byte[] toSvg(String source) {
		return dot(source,"svg");
	}
	
	public byte[] dot(String source , String type) { 			
		ProcessBuilder builder = new ProcessBuilder("dot","-T" + type);
		try {
			Process process = builder.start();
			try (OutputStream stdIn = process.getOutputStream()) {
				stdIn.write(source.getBytes());
			}			
			try (InputStream stdOut = process.getInputStream()) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] data = new byte[1<<14];
				int nRead;
				while ((nRead = stdOut.read(data)) != -1) {
					buffer.write(data,0,nRead);
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
