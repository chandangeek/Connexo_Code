/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.extended.DirectionOfFlow', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/flowdirection',
        reader: {
            type: 'json',
            root: 'flowdirectionCodes'
        },
        limitParam: false
    }
});

