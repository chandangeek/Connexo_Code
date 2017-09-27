/*
 *  Copyright text to : Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.ExportTasks', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.DataExportTask',

    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask/usagepoint/{usagePointId}/{purposeId}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataExportTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});