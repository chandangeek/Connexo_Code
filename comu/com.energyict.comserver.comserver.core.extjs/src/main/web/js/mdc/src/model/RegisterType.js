Ext.define('Mdc.model.RegisterType', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'isLinkedByDeviceType', type: 'boolean', useNull: true},
        {name: 'isLinkedByActiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'isLinkedByInactiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'isCumulative', type: 'boolean', useNull: true},
        'readingType',
        'collectedReadingType',
        'calculatedReadingType',
        'possibleCalculatedReadingTypes',
        {name: 'name', type: 'string', persist: false, mapping: 'readingType.fullAliasName'}
    ],
    idProperty: 'id',
    associations: [
        {
            name: 'readingType',
            associationKey: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        },
        {
            name: 'collectedReadingType',
            associationKey: 'collectedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            foreignKey: 'collectedReadingType'
        },
        {
            name: 'calculatedReadingType',
            associationKey: 'calculatedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            foreignKey: 'calculatedReadingType'
        },
        {
            name: 'possibleCalculatedReadingTypes',
            associationKey: 'possibleCalculatedReadingTypes',
            type: 'hasMany',
            model: 'Mdc.model.ReadingType',
            foreignKey: 'possibleCalculatedReadingTypes'
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mds/registertypes'
    }
});