package com.elster.jupiter.metering;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.devtools.test.analysis.MethodInvocationChecker;
import com.elster.jupiter.metering.impl.test.BaseReadingImpl;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

public class NewDateCheckTest {

	@Test
	public void test() throws NoSuchMethodException, SecurityException, IOException {
		Set<ClassPath.ClassInfo> classInfos = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("com.elster");
		assertThat(classInfos).isNotEmpty();
		List<ClassPath.ClassInfo> suspects = new ArrayList<>();
		for (ClassPath.ClassInfo classInfo : classInfos) {
			if (check(classInfo)) {
				if (MethodInvocationChecker.invokes(classInfo,Date.class.getConstructor())) {
					suspects.add(classInfo);
				}
			}
		}
		assertThat(suspects).isEmpty();
	}
	
	private static Set<Class<?>> excludes = ImmutableSet.of(Status.class, BaseReadingImpl.class, DefaultClock.class);
	
	private boolean check(ClassPath.ClassInfo classInfo) {
		if (classInfo.getName().endsWith("Test")) {
			return false;
		}
		try {
			return !excludes.contains(classInfo.load());
		} catch (Throwable ex) {
			return false;
		}
	}
}

