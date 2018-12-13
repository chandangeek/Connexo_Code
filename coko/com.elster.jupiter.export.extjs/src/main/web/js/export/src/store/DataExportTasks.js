/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DataExportTasks', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataExportTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataExportTasks'
        }
    }
});
