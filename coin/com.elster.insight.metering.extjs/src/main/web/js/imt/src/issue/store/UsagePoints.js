/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.issue.store.UsagePoints', {
    extend: 'Ext.data.Store',
    model: 'Imt.issue.model.UsagePoint',
    pageSize: 50,
    autoLoad: false
});