/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueType',
    pageSize: 10,
    autoLoad: false
});