Ext.define('Mdc.model.DeviceRegister', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'readingType', persist:false},
        {name: 'registerType', type:'number', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'type', type: 'string', useNull: true},
        {name: 'isCumulative', type: 'boolean'},
        {name: 'numberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overruledNumberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overflow', type: 'number', useNull: true},
        {name: 'overruledOverflow', type: 'number', useNull: true}
    ],
    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{deviceId}/registers/',
        reader: {
            type: 'json'
        }
    }
});