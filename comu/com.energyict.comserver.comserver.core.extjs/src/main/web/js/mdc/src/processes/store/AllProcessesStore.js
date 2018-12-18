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
        urlTpl: '/api/ddr/flowprocesses/processes?variableid={variableid}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processHistories'
        },
        setUrl: function (variableid) {
            this.url = this.urlTpl.replace('{variableid}', variableid);
        }
    }
});
