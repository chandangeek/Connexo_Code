package com.elster.jupiter.devtools.test.analysis;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;

import com.google.common.reflect.ClassPath;

public class MethodInvocationChecker {

	private final ClassPath.ClassInfo classInfo;
	private final String className;
	private final String methodName;
	private final String descriptor;
	private boolean found;
	
	MethodInvocationChecker(ClassPath.ClassInfo classInfo, String className , String methodName, String descriptor) {
		this.classInfo = classInfo;
		this.className = className.replace(".","/");
		this.methodName = methodName;
		this.descriptor = descriptor;
		
	}
	
	public static boolean invokes(ClassPath.ClassInfo classInfo, java.lang.reflect.Method method) throws IOException {
		return new MethodInvocationChecker(
				classInfo,
				method.getDeclaringClass().getName(),
				method.getName(),
				Method.getMethod(method).getDescriptor()).invokes();
	}
	
	public static boolean invokes(ClassPath.ClassInfo classInfo, Constructor<?> constructor) throws IOException {
		Method method = Method.getMethod(constructor);
		return new MethodInvocationChecker(
				classInfo,
				constructor.getDeclaringClass().getName(), 
				"<init>", 
				method.getDescriptor()).invokes();
	}
	
	boolean invokes() throws IOException {
		new ClassReader(classInfo.getName()).accept(new MethodInvocationClassVisitor(),0);
		return found;
	}
	
	private class MethodInvocationClassVisitor extends ClassVisitor {

		public MethodInvocationClassVisitor() {
			super(Opcodes.ASM4);
		}
	
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new MethodMatcher();
		}
	
	}
		
	private class MethodMatcher extends MethodVisitor {

		public MethodMatcher() {
			super(Opcodes.ASM4);
		}
		
		@Override
		public void visitMethodInsn(int opCode, String owner , String name , String desc) {
			if (owner.equals(className) && name.equals(methodName) && desc.equals(descriptor)) {
				found = true;
			}
		}
	}	
}
