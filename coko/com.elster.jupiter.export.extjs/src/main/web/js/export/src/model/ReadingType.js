/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.ReadingType', {
    extend: 'Ext.data.Model',

    fields: [
        'mRID'
    ],

    associations: [
        {
            type: 'belongsTo',
            model: 'Dxp.model.AddDataExportTask',
            name: 'readingTypes'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});
