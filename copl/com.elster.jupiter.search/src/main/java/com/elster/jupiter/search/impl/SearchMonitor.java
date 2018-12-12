/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.util.time.ExecutionTimer;

/**
 * Defines the interface for a component that will monitor the
 * searches that are being executed on the Connexo platform.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-09 (08:48)
 */
public interface SearchMonitor {

    void searchDomainRegistered(SearchDomain searchDomain);

    void searchDomainUnregistered(SearchDomain searchDomain);

    ExecutionTimer searchTimer(SearchDomain searchDomain);

    ExecutionTimer countTimer(SearchDomain searchDomain);

}