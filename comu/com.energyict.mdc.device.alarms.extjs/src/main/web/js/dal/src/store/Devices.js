/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.Device',
    pageSize: 50,
    autoLoad: false
});