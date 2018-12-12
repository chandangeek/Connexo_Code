/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.model.DataQuality', {
    extend:'Ext.data.Model',
    requires: [
        'Ddv.model.ItemsPerType'
    ],
    fields: [
        'amountOfAdded',
        'amountOfConfirmed',
        'amountOfEdited',
        'amountOfEstimates',
        'amountOfInformatives',
        'amountOfRemoved',
        'amountOfSuspects',
        'amountOfTotalEdited',
        'channelSuspects',
        'deviceConfig',
        'deviceName',
        'deviceSerialNumber',
        'deviceType',
        'lastSuspect',
        'registerSuspects'
    ],

    associations: [
        {
            name: 'estimatesPerEstimator',
            type: 'hasMany',
            model: 'Ddv.model.ItemsPerType',
            associationKey: 'estimatesPerEstimator'
        },
        {
            name: 'suspectsPerValidator',
            type: 'hasMany',
            model: 'Ddv.model.ItemsPerType',
            associationKey: 'suspectsPerValidator'
        }
    ]
});