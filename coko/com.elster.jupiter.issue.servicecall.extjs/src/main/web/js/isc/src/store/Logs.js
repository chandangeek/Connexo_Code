/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.store.Logs', {
    extend: 'Ext.data.Store',
    fields: [
        { name: 'timestamp', type: 'number' },
        { name: 'details', type: 'string' },
        { name: 'logLevel', type: 'string' }
    ]
});
