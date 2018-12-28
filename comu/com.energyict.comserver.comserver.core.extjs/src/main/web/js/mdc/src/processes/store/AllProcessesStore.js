/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.AllProcessesStore', {
    extend: 'Ext.data.Store',
    model: 'Mdc.processes.model.ProcessGeneralModel',
    storeId: 'AllProcessesStore',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/flowprocesses/processes',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processHistories'
        }
    }
});
