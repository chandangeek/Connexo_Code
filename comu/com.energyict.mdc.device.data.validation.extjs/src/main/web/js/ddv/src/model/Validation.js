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
        'serialNumber',
        'deviceType',
        'deviceConfig',
        'configuration',
        'allDataValidated',
        'registers',
        'channels',
        'lastValidation',
        'lastSuspect',
        'typeOfSuspects'
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