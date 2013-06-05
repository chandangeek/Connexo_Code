package com.elster.jupiter.orm;

import com.google.common.base.Optional;

public interface OrmService {
	// standard api
	Optional<DataModel> getDataModel(String name);
	// api for modules with dynamic orm mapping or client module install time
	DataModel newDataModel(String name, String description);
}