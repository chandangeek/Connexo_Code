/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueDevices', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueDevice',
    pageSize: 50,
    autoLoad: false
});