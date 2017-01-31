/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.test.analysis;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class SlocMetricTest {

	@Test
	public void testSloc() throws IOException {
		Set<ClassPath.ClassInfo> classInfos = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("com.elster");
		int sloc = 0;
		for (ClassPath.ClassInfo classInfo : classInfos) {
			sloc += SlocMetric.sloc(classInfo.getName());
		}
		assertThat(sloc).isGreaterThan(10);
	}

}