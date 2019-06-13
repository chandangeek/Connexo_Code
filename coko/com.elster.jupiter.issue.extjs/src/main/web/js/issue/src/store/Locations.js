/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.Locations', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Location',
    pageSize: 50,
    autoLoad: false
});