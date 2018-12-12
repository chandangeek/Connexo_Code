/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.extended.ArgumentDenominator', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/argumentdenominator',
        reader: {
            type: 'json',
            root: 'argumentdenominatorCodes'
        },
        limitParam: false
    }
});
