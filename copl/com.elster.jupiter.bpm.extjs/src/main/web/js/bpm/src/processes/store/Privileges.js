/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.store.Privileges', {
    extend: 'Ext.data.Store',
    model: 'Bpm.processes.model.Privilege',
    autoLoad: false,
    proxy: {
        type: 'rest',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '/api/bpm/runtime/processes/privileges',
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});
