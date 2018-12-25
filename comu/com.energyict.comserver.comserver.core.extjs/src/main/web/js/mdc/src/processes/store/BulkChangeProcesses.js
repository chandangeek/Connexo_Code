/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.BulkChangeProcesses', {
    extend: 'Ext.data.Store',
    model: 'Mdc.processes.model.BulkChangeProcesses',
//    model: 'Isu.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});