package com.elster.jupiter.orm;

import com.elster.jupiter.orm.associations.RefAny;
import com.google.common.base.Optional;

import java.util.List;

/*
 * OSGI ORM Service
 * 
 * Bundles interact with the ORM Service through a DataModel object.
 * 
 * Bundles use the newDataModel api to get an empty instance.
 * After configuring the dataModel, the client bundle invokes register to activate the DataModel
 * 
 * DataModel are identified by its name. The name is a unique 3 character String and its typically 
 * used to prefix the table names used by a DataModel.  
 *
 */
public interface OrmService {
	public static final String COMPONENTNAME = "ORM";

	DataModel newDataModel(String name, String description);
	RefAny createRefAny(String componentName, String tableName, Object... key);

	/*
	 * Only for applications that need to document the data model 
	 */
	List<? extends DataModel> getDataModels();
	Optional<? extends DataModel> getDataModel(String name);

	/*
	 *  
	 */
    void invalidateCache(String componentName, String tableName);
}