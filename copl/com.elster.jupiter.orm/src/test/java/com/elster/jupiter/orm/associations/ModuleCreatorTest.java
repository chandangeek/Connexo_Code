package com.elster.jupiter.orm.associations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.StopWatch;
import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Module;

@RunWith(MockitoJUnitRunner.class)
public class ModuleCreatorTest {
	
	@Mock
	private DataModel dataModel;
	
	private static class Parent {
		@Inject
		protected List<ModuleCreatorTest> parentList;
		@Inject
		protected Reference<ModuleCreatorTest> parentReference;
	}
	
	private static class Child extends Parent {
		@Inject
		private List<ModuleCreatorTest> childList;
		@Inject
		private Reference<ModuleCreatorTest> childReference;
		private List<ModuleCreatorTest> bisList;
		private Reference<ModuleCreatorTest> bisReference;
	}

	@Test
	public void test() {
		Module module = ModuleCreator.create(Child.class);
		Child instance = Guice.createInjector(module).getInstance(Child.class);
		assertThat(instance.childList).hasSize(0);
		assertThat(instance.childReference.orNull()).isNull();
		assertThat(instance.parentList).hasSize(0);
		assertThat(instance.parentReference.orNull()).isNull();
		assertThat(instance.bisList).isNull();
		assertThat(instance.bisReference).isNull();
	}
	
	@Test 
	public void testDuplicates() {
		Module module = ModuleCreator.create(Parent.class, Child.class);
		Child instance = Guice.createInjector(module).getInstance(Child.class);
		assertThat(instance.childList).hasSize(0);
		assertThat(instance.parentList).hasSize(0);
	}
	
	@Test
	public void testPerformance() throws IOException {
		StopWatch stopWatch = new StopWatch(false);
		Set<ClassPath.ClassInfo> allClassesInfo = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClasses();
		System.out.println("Finding: " + allClassesInfo.size() + " in " + stopWatch.lap());
		List<Class<?>> allClasses = new ArrayList<>(allClassesInfo.size());	
		for (ClassPath.ClassInfo each : allClassesInfo) {
			try {
				allClasses.add(each.load());
			} catch(NoClassDefFoundError e) {
			}
		}
		System.out.println("Loaded: " + allClasses.size() + " in " + stopWatch.lap());
		Module module = ModuleCreator.create(allClasses.toArray(new Class<?>[allClasses.size()]));
		System.out.println("Inspected: " + allClasses.size() + " in " + stopWatch.lap());
	}
	
	
}
