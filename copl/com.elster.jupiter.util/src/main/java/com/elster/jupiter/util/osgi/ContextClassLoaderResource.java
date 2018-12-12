/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.osgi;

public class ContextClassLoaderResource implements AutoCloseable {

	private final ClassLoader oldLoader;
	
	private ContextClassLoaderResource(ClassLoader newLoader) {
		oldLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(newLoader);
	}
	
	@Override
	public void close() {
		Thread.currentThread().setContextClassLoader(oldLoader);
	}
	
	public static ContextClassLoaderResource of(Class<?> clazz) {
		return new ContextClassLoaderResource(clazz.getClassLoader());
	}

}
