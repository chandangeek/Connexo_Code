Ext.define('Mdc.model.RegisterConfiguration', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'obisCodeDescription', type: 'string', useNull: true},
        {name: 'unitOfMeasure', type: 'string', useNull: true},
        {name: 'numberOfDigits', type: 'number', useNull: true},
        {name: 'numberOfFractionDigits', type: 'number', useNull: true},
        {name: 'multiplier', type: 'number', useNull: true},
        {name: 'overflow', type: 'number', useNull: true},
        {name: 'timeOfUse', type:'number', useNull: true},
        {name: 'registerMapping', type:'number', useNull: true},
        {name: 'multiplierMode', type:'string', useNull: true},
        'readingType'
    ],
    associations: [
        {name: 'readingType', type: 'hasOne', model: 'Mdc.model.ReadingType', associationKey: 'readingType',
            getterName: 'getReadingType', setterName: 'setReadingType', foreignKey: 'readingType'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registerconfigurations'
    }
});