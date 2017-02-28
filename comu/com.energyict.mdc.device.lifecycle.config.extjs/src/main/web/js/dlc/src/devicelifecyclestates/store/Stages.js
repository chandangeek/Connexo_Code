/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.store.Stages', {
    extend: 'Ext.data.Store',
    fields: ['id', 'displayValue'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/stages',
        reader: {
            type: 'json'
        }
    }
});
