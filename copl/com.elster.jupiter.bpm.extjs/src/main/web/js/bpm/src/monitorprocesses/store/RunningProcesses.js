/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.store.RunningProcesses', {
    extend: 'Ext.data.Store',
    model: 'Bpm.monitorprocesses.model.RunningProcess',

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/runningprocesses?variableid={variableid}&variablevalue={variablevalue}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        setUrl: function (variableid, variablevalue) {
            this.url = this.urlTpl.replace('{variableid}', variableid)
                .replace('{variablevalue}', encodeURIComponent(variablevalue));
        }
    }
});
