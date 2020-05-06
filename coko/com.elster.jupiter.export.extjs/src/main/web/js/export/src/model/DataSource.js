/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'name', 'active', 'serialNumber', 'readingType', 'occurrenceId', 'connectionState', 'purpose',
        {
            name: 'lastExportedChangedData',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'lastExportedNewData',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
