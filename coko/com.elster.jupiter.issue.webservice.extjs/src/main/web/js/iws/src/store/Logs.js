/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.store.Logs', {
    extend: 'Ext.data.Store',
    fields: [
        { name: 'timestamp', type: 'auto' },
        { name: 'message', type: 'auto' },
        { name: 'logLevel', type: 'auto' }
    ]
});
