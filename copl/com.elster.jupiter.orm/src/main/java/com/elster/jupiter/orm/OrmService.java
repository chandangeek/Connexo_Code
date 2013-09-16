package com.elster.jupiter.orm;

import com.google.common.base.Optional;

/*
 * OSGI ORM Service
 * 
 * Bundles interact with the ORM Service through a DataModel object.
 * There are two ways to obtain a DataModel
 * 
 * If the mappings are already stored in the ORM component's tables, an application can
 * use getDataModel.
 * 
 * Alternatively an application can obtain an empty DataModel with new DataModel, and add 
 * mappings to it using the DataModel API
 * 
 * DataModel are identified by its name. The name is a unique 3 character String and its typically 
 * used to prefix the table names used by a DataModel. 
 *
 */
public interface OrmService {
	Optional<DataModel> getDataModel(String name);
	DataModel newDataModel(String name, String description);
}