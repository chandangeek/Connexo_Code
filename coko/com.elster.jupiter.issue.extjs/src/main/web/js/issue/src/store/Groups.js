/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.Groups', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Group',
    pageSize: 10,
    autoLoad: false
});