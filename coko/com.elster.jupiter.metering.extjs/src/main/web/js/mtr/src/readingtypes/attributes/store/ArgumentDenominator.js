/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.attributes.store.ArgumentDenominator',{
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
