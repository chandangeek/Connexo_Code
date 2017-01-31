/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.attributes.store.InterharmonicDenominator',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/interharmonicdenominator',
        reader: {
            type: 'json',
            root: 'interharmonicdenominatorCodes'
        },
        limitParam: false
    }
});
