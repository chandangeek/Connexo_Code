package com.elster.jupiter.data.lifecycle.impl;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryName;
import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;

@Component(name="com.elster.jupiter.data.lifecycle")
public class LifeCycleServiceImpl implements LifeCycleService, InstallService {
	
	private volatile DataModel dataModel;
	
	public LifeCycleServiceImpl() {	
	}

	@Inject
	LifeCycleServiceImpl(OrmService ormService) {
		setOrmService(ormService);
		if (!dataModel.isInstalled()) {
			install();
		}
	}
	
	@Override
	public void install() {		
		dataModel.install(true, true);
		for (LifeCycleCategoryName category : LifeCycleCategoryName.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl().init(category);
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
		dataModel.register();
	}

	@Override
	public List<LifeCycleCategory> getCategories() {
		return dataModel.stream(LifeCycleCategory.class).select();
	}
	
}
