/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.FlaggedDevices', {
    extend: 'Ext.data.Store',
    storeId: 'FlaggedDevices',
    requires: ['Dsh.model.FlaggedDevice'],
    model: 'Dsh.model.FlaggedDevice',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/mylabeleddevices?category=mdc.label.category.favorites',
        pageParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            root: 'myLabeledDevices'
        }
    }
});