package com.elster.jupiter.launcher;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import com.google.common.reflect.ClassPath;
import com.elster.jupiter.devtools.test.analysis.*;


public class Analyzer {

	public static void main(String args[]) {
		Set<ClassPath.ClassInfo> classInfos = null;
		try {
			classInfos = ClassPath.from(Analyzer.class.getClassLoader()).getTopLevelClassesRecursive("com.elster");
		} catch (IOException e) {
			e.printStackTrace();
		}
		int tables = 0;
		int classes = 0;
		int restapis = 0;
		int methods = 0;
		int sloc = 0;
		for (ClassPath.ClassInfo each : classInfos) {
			Class<?> clazz = each.load();
			if (!each.getSimpleName().endsWith("Test") || each.getName().contains(".test.")) {
				classes++;
			} else {
				continue;
			}
			System.out.println(clazz.getName());
			if (each.getSimpleName().equals("TableSpecs")) {
				tables += clazz.getEnumConstants().length;
			}
			for (Method method : clazz.getDeclaredMethods()) {
				methods++;
				if (method.isAnnotationPresent(GET.class) 
						|| method.isAnnotationPresent(PUT.class)
						|| method.isAnnotationPresent(POST.class)
						|| method.isAnnotationPresent(DELETE.class)) {
					restapis++;
				}
			}
			try {
				sloc += SlocMetric.sloc(each.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Number of classes: " + classes);
		System.out.println("Number of methods: " + methods);
		System.out.println("Number of tables: " + tables);
		System.out.println("Number of rest api: " + restapis);
		System.out.println("Sloc: " + sloc);
	}
}
