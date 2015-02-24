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
      //  {name: 'unitOfMeasure', useNull: true},
        {name: 'numberOfDigits', type: 'number', useNull: true},
        {name: 'numberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overflow', type: 'number', useNull: true},
        {name: 'timeOfUse', type:'number', useNull: true},
        {name: 'registerType', type:'number', useNull: true},
        {name: 'readingType', persist:false},
        {name: 'asText', type:'boolean'}
    ],
    associations: [
        {name: 'readingType', type: 'hasOne', model: 'Mdc.model.ReadingType', associationKey: 'readingType',
            getterName: 'getReadingType', setterName: 'setReadingType', foreignKey: 'readingType'},
        {name: 'unitOfMeasure', type: 'hasOne', model: 'Mdc.model.UnitOfMeasure', associationKey: 'unitOfMeasure',
            getterName: 'getUnitOfMeasure', setterName: 'setUnitOfMeasure', foreignKey: 'unitOfMeasure'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registerconfigurations'
    }
});