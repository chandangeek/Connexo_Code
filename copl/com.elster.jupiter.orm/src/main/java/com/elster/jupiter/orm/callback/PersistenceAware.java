/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.callback;

import aQute.bnd.annotation.ConsumerType;

/*
 *  If a persistent object implements this interface, the postLoad method
 *  will be called after the object's fields have been set to the database values
 *  
 *  In contrast to JPA, postLoad implementations can perform actions that trigger
 *  additional database queries.
 *  
 *  Mainly used for cached objects to make them thread safe  
 *  and avoid lazy initialization of associated objects.
 *      
 */
@ConsumerType
public interface PersistenceAware {
	void postLoad();
}
