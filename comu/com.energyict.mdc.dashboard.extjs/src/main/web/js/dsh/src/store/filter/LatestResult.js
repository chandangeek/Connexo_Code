/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.filter.LatestResult', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comsessionsuccessindicators',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});

