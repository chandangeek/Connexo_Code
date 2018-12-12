/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorissueprocesses.store.IssueProcesses', {
    extend: 'Ext.data.Store',
    model: 'Bpm.monitorissueprocesses.model.IssueProcess',
    autoLoad: false,
    proxy: 'memory',

    proxy: {
        type: 'rest',
        urlTpl: '/api/idc/issues/{issueId}/processes?variableid=issueId&variablevalue={Id}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        setUrl: function (issueId) {
            this.url = this.urlTpl.replace('{issueId}', issueId)
                .replace('{Id}', issueId);
        }
    }
});
