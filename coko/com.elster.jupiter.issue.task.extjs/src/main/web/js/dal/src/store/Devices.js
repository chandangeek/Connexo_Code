/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.Device',
    pageSize: 50,
    autoLoad: false
});