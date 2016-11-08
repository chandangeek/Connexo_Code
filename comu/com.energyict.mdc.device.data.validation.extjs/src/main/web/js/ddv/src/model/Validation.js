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
        'name',
        'serialNumber',
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