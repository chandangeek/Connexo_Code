/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.ReadingType'],
    model: 'Mdc.model.ReadingType',
    storeId: 'ReadingTypes',
    remoteFilter: true
});