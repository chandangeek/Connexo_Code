package com.elster.jupiter.orm.callback;

/*
 *  If a persistent object implements this interface, the postLoad method
 *  will be called after the object's fields have been set to the database values
 *  
 *  In contrast to JPA, postLoad implementations can perform actions that trigger
 *  additional database queries.
 *  
 *  Mainly used for object using the orm.cache bundle, to make them thread safe  
 *  and avoid lazy initialization of associated objects.
 *      
 */
public interface PersistenceAware {
	void postLoad();
}
