/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/events")
public class EventResource {

	@Inject
	private Events events;
	
	@GET
	@Path("/topics/{topic:.+}")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public long getCount(@PathParam("topic") String topic) {
		return events.getCount(topic);
	}
	
	@GET
	@Path("/topics")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public List<String> getTopics() {
		return events.getTopics();
	}
	
	@GET
	@Path("/cimfiles")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public List<String> getFiles() {
		File directory = new File(".");
		File[] files = directory.listFiles(new FilenameFilter()  {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("xml");
			}
		});
		List<String> result = new ArrayList<>(files.length);
		for (File file :files ) {
			result.add(file.getName());
		}
		return result;
	}
	
	@GET
	@Path("/cimfiles/{name}")
	@Produces(MediaType.APPLICATION_XML)
	public String getFile(@PathParam("name") String name) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader("./" + name))) {
			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append("\n");
				line = reader.readLine();
			}
			return buffer.toString();
		}
	}
}
