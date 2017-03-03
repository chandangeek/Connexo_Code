/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.model.TypeOfSuspects', {
    extend:'Ext.data.Model',
    fields: [
        'name'
    ]
});

Ext.define('Ddv.model.Validation', {
    extend:'Ext.data.Model',
    fields: [
        'mrid',
        'deviceName',
        'deviceSerialNumber',
        'deviceType',
        'deviceConfig',
        'amountOfSuspects',
        'allDataValidated',
        'registerSuspects',
        'channelSuspects',
        'lastValidation',
        'lastSuspect',
        'typeOfSuspects',
        'thresholdValidator',
        'missingValuesValidator',
        'readingQualitiesValidator',
        'registerIncreaseValidator'
    ],

    associations: [
        {
            type: 'hasMany',
            model: 'Ddv.model.TypeOfSuspects',
            associationKey: 'typeOfSuspects',
            name: 'typeOfSuspects'
        }
    ]

});