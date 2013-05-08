package com.elster.jupiter.orm;

public interface OrmService {
	// standard api
	DataModel getDataModel(String componentName);
	// api for modules with dynamic orm mapping or client module install time
	DataModel newDataModel(String name, String description);
}