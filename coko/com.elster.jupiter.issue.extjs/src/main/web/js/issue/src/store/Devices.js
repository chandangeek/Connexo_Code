/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Device',
    pageSize: 50,
    autoLoad: false
});