package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.RefAny;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
@ProviderType
public interface OrmService {
    public static final String COMPONENTNAME = "ORM";

    public static final String EXISTING_TABLES_DATA_MODEL = "ORA";

    /*
     * creates a new Data Model with the given name and description
     * name should be an unique three letter acronym
     */
    DataModel newDataModel(String name, String description);

    /*
     * represent the given primary key information as a RefAny
     */
    RefAny createRefAny(String componentName, String tableName, Object... key);

    /*
     * Obtain all registered dataModels
     */
    List<? extends DataModel> getDataModels();

    /*
     * Gets the dataModel with the given name
     */
    Optional<? extends DataModel> getDataModel(String name);

    /*
     * Cache coherence between app servers
     */
    void invalidateCache(String componentName, String tableName);

    /*
     * remove all journal entries up to a given instant
     */
    void dropJournal(Instant upTo, Logger logger);
    /*
     * remove all data before upTo for the given life cycle class
     */
	void dropAuto(LifeCycleClass lifeCycleClass, Instant upTo, Logger logger);
	/*
	 * create partitions for all range partitioned tables up to the first argument
	 */
	void createPartitions(Instant upTo, Logger logger);

}