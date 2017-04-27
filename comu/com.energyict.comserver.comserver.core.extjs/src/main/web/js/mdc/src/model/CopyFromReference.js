/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CopyFromReference', {
    extend: 'Ext.data.Model',

    fields: [
        'referenceDevice',
        'readingType',
        'startDate',
        'allowSuspectDate',
        'completePeriod',
        'intervals'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{usagePointId}/channels/{purposeId}/data/copyfromreference',
        reader: {
            type: 'json'
        }
    }
});
