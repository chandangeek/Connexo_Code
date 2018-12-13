/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableReadingTypesForRegisterType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ReadingType'
    ],
    model: 'Mdc.model.ReadingType',
    storeId: 'AvailableReadingTypesForRegisterType',
    proxy: {
        type: 'ajax',
        url: '/api/mds/unusedreadingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});