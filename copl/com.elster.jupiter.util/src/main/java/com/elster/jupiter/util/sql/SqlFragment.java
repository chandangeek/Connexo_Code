/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * SqlFragment encapsulates both the sql fragment, 
 * as the related the PreparedStatement setXXX methods
 */
public interface SqlFragment {
	/**
	 * bind values to host variables, using PreparedStatement setXXX methods.
	 * Will be called from SqlBuilder.prepare
	 * @param statement
	 * @param position start position
	 * @return position + number of bound variables
	 * @throws SQLException
	 */
	int bind(PreparedStatement statement , int position) throws SQLException;

	/**
	 * Will be called from SqlBuilder.add.
	 * @return the fragment's sql text, which typically contains ? place holders for bound variables  
	 */
    String getText();
}
