/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Order;
import java.util.List;

public interface RestQuery<T> {
    
		
	List<T> select(QueryParameters queryParameters, Order... orders);

	@Deprecated
	List<T> select(QueryParameters queryParameters, String order, String ... orders);
	

}
