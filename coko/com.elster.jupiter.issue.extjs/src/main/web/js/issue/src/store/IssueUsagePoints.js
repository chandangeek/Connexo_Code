/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueUsagePoints', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueUsagePoints',
    pageSize: 50,
    autoLoad: false
});