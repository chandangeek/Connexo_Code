Ext.define('Mdc.model.Channel', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.RegisterType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'overflowValue', type: 'integer', useNull: true},
        {name: 'unitOfMeasure', type: 'auto', useNull: true},
        'measurementType',
        {name: 'readingType', mapping: 'measurementType.readingType'},
        {name: 'calculatedReadingType', mapping: 'measurementType.calculatedReadingType'},
        {name: 'nbrOfFractionDigits', type: 'int'}
    ],
    idProperty: 'id',
    associations: [
        {name: 'measurementType', type: 'hasOne', model: 'Mdc.model.RegisterType', associationKey: 'measurementType',
            getterName: 'getMeasurementType', setterName: 'setMeasurementType', foreignKey: 'measurementType'}
    ]
});
