package com.elster.jupiter.devtools.test.analysis;

import com.google.common.reflect.ClassPath;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class MethodInvocationCheckerTest {
	
	@Test
	public void testMethod() throws IOException, NoSuchMethodException, SecurityException {
		Set<ClassPath.ClassInfo> classInfos = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("com.elster");
		assertThat(classInfos).isNotEmpty();
		boolean found = false;
		for (ClassPath.ClassInfo classInfo : classInfos) {
			found |= MethodInvocationChecker.invokes(classInfo,MethodInvocationChecker.class.getMethod("invokes",ClassPath.ClassInfo.class,Method.class));
		}
		assertThat(found).isTrue();
	}

	@Test
	public void testConstructor() throws IOException, NoSuchMethodException, SecurityException {
		new MethodInvocationCheckerTest(); 
		Set<ClassPath.ClassInfo> classInfos = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("com.elster");
		assertThat(classInfos).isNotEmpty();
		boolean found = false;
		for (ClassPath.ClassInfo classInfo : classInfos) {
			found |= MethodInvocationChecker.invokes(classInfo,MethodInvocationCheckerTest.class.getConstructor());
		}
		assertThat(found).isTrue();
	}
	
	@Test
    @Ignore
	public void testSloc() throws IOException {
		Set<ClassPath.ClassInfo> classInfos = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("com.elster");
		int sloc = 0;
		for (ClassPath.ClassInfo classInfo : classInfos) {
			sloc += SlocMetric.sloc(classInfo.getName());
		}
		assertThat(sloc).isGreaterThan(10);
	}
}
