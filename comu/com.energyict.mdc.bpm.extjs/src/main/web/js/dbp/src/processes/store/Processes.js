/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.store.Processes', {
    extend: 'Ext.data.Store',
    model: 'Dbp.processes.model.Process',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/allprocesses',
        reader: {
            type: 'json',
            root: 'processes'
        }
    }
});
