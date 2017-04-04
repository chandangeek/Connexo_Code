/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.RegisterReading', {
    extend: 'Uni.model.Version',
    fields: [
        {
            name: 'isCumulative',
            type: 'boolean'
        },
        {
            name: 'hasEvent',
            type: 'boolean'
        },
        {
            name: 'isBilling',
            type: 'boolean'
        },
        'value', 'measurementTime', 'measurementPeriod', 'eventDate', 'collectedValue', 'delta', 'deltaValue', 'readingTime', 'readingQualities', 'dataValidated',
        {
            name: 'validationResult',
            mapping: function (data) {
                var result = 'NOT_VALIDATED';

                switch (data) {
                    case 'validationStatus.suspect':
                        result = 'SUSPECT';
                        break;
                    case 'validationStatus.ok':
                        result = 'OK';
                        break;
                }

                return result;
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