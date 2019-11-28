/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.store.Associations', {
    extend: 'Ext.data.Store',
    model: 'Bpm.processes.model.Association',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: false,
    proxy: {
        type: 'rest',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '/api/bpm/runtime/process/associations',
        reader: {
            type: 'json',
            root: 'associations'
        }
    }

});
