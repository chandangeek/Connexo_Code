/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/**
 * Counterpart to the {@link DdlDifference} that only offers a DDL statement for execution, this Difference allows code
 * to execute to implement a table update
 */
@ProviderType
public interface DifferenceCommand extends Difference {
    /**
     * Allows Java code execution in order to update a table.
     * The execute() method will be executed in a transaction
     */
    public void execute();
}
