/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.DirectionOfFlow', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/flowdirection/{filter}',
        reader: {
            type: 'json',
            root: 'flowdirectionCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
