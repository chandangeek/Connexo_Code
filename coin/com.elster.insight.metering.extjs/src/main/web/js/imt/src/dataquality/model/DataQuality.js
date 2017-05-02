/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.model.DataQuality', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.dataquality.model.ItemsPerType'
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
        'amountOfProjected',
        'channelSuspects',
        'isEffectiveConfiguration',
        'lastSuspect',
        'metrologyConfiguration',
        'metrologyContract',
        'registerSuspects',
        'serviceCategory',
        'usagePointName'
    ],

    associations: [
        {
            name: 'estimatesPerEstimator',
            type: 'hasMany',
            model: 'Imt.dataquality.model.ItemsPerType',
            associationKey: 'estimatesPerEstimator'
        },
        {
            name: 'suspectsPerValidator',
            type: 'hasMany',
            model: 'Imt.dataquality.model.ItemsPerType',
            associationKey: 'suspectsPerValidator'
        }
    ]
});