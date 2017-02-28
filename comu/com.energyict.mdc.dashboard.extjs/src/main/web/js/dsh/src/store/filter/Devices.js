/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Dsh.store.filter.Devices', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.Device',
    pageSize: 50,
    autoLoad: false
});