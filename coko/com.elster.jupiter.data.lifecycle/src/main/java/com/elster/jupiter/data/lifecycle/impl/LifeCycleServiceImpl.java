package com.elster.jupiter.data.lifecycle.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.AbstractModule;

@Component(name="com.elster.jupiter.data.lifecycle")
public class LifeCycleServiceImpl implements LifeCycleService, InstallService {
	
	private volatile DataModel dataModel;
	
	public LifeCycleServiceImpl() {	
	}

	@Inject
	public LifeCycleServiceImpl(OrmService ormService) {
		setOrmService(ormService);
		if (!dataModel.isInstalled()) {
			install();
		}
	}
	
	@Override
	public void install() {		
		dataModel.install(true, true);
		for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel).init(category);
			dataModel.persist(newCategory);
		}
	}

	@Override
	public List<String> getPrerequisiteModules() {
		return Arrays.asList("ORM");
	}

	@Reference
	public void setOrmService(OrmService ormService) {
		dataModel = ormService.newDataModel("LFC", "Data Life Cycle Management");
		for (TableSpecs table : TableSpecs.values()) {
			table.addTo(dataModel);
		}
		dataModel.register(new AbstractModule() {			
			@Override
			protected void configure() {
				bind(DataModel.class).toInstance(dataModel);
			}
		});
	}

	@Override
	public List<LifeCycleCategory> getCategories() {
		return dataModel.stream(LifeCycleCategory.class)
			.sorted(Comparator.comparing(LifeCycleCategory::getKind))
			.collect(Collectors.toList());
	}
	
}
