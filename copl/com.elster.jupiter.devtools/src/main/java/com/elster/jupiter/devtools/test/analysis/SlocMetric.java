/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.test.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

public class SlocMetric {

	private final String className;
	private int sloc;


	SlocMetric(String className) {
		this.className = className.replace(".", "/");
	}

	public static int sloc(String className) throws IOException {
		return new SlocMetric(className).sloc();
	}

	int sloc() throws IOException {
		new ClassReader(className).accept(new SlocVisitor(),0);
		System.out.println(className + " : " + sloc);
		return sloc;
	}

	private class SlocVisitor extends ClassVisitor {

		private SlocVisitor() {
			super(Opcodes.ASM4);

		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new MethodSloc();
		}

		@Override
		public void visitInnerClass(String name, String outer , String inner, int access) {
			if (name.startsWith(className) && !name.equals(className)) {
				try {
					int slocMax = SlocMetric.sloc(name);
					if (slocMax > sloc) {
						sloc = slocMax;
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}

	private class MethodSloc extends MethodVisitor {

		private MethodSloc() {
			super(Opcodes.ASM4);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			if (sloc < line) {
				sloc = line;
			}
		}
	}
}
