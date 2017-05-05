/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.CopyFromReference', {
    extend: 'Ext.data.Model',

    fields: [
        'referenceUsagePoint',
        'referencePurpose',
        'readingType',
        'startDate',
        'projectedValue',
        'allowSuspectData',
        'completePeriod',
        'intervals'
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/channelData/copyfromreference',
        reader: {
            type: 'json'
        }
    }
});
