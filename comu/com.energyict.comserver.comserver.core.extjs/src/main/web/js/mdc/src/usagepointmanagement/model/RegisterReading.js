/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.RegisterReading', {
    extend: 'Uni.model.Version',
    fields: [
        'value', 'measurementTime', 'readingTime', 'readingQualities', 'validationResult', 'dataValidated',
        // {
        //     name: 'id',
        //     mapping: 'interval.end'
        // },
        {
            name: 'dataValidated',
            mapping: function (data) {
                return data ? 'SUSPECT': 'NOT_VALIDATED';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/registers/{registerId}/data',
        reader: {
            type: 'json'
        }
    }
});