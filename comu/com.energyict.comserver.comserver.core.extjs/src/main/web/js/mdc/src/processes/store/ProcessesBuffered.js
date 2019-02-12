/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.ProcessesBuffered', {
    extend: 'Mdc.processes.store.AllProcessesStore',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});
