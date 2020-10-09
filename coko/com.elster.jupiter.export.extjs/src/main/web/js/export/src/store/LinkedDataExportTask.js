/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.LinkedDataExportTask', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.LinkedDataExportTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/fields/taskstopair',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'tasksToPair'
        }
    }
});