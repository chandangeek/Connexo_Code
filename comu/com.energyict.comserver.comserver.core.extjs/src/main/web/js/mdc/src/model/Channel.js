Ext.define('Mdc.model.Channel', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.RegisterType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'overflowValue', type: 'integer', useNull: true},
        'measurementType',
        {name: 'readingType', mapping: 'measurementType.readingType'},
        'collectedReadingType',
        'calculatedReadingType',
        'useMultiplier',
        {name: 'nbrOfFractionDigits', type: 'int'},
        {
            name: 'registerTypeName',
            type: 'string',
            persist: false,
            mapping: 'measurementType.readingType.fullAliasName'
        }
    ],
    idProperty: 'id',
    associations: [
        {
            name: 'measurementType',
            type: 'hasOne',
            model: 'Mdc.model.RegisterType',
            associationKey: 'measurementType',
            getterName: 'getMeasurementType',
            setterName: 'setMeasurementType',
            foreignKey: 'measurementType'
        },
        {
            name: 'collectedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'collectedReadingType',
            getterName: 'getCollectedReadingType',
            setterName: 'setCollectedReadingType',
            foreignKey: 'collectedReadingType'
        },
        {
            name: 'calculatedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'calculatedReadingType',
            getterName: 'getCalculatedReadingType',
            setterName: 'setCalculatedReadingType',
            foreignKey: 'calculatedReadingType'
        }
    ]
});
