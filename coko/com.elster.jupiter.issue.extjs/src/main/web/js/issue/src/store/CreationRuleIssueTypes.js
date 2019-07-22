/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.CreationRuleIssueTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleIssueType',
    pageSize: 10,
    autoLoad: false
});