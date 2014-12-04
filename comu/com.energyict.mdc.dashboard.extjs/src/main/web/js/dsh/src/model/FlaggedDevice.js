Ext.define('Dsh.model.FlaggedDevice', {
    extend: 'Ext.data.Model',
    idProperty: 'mRID',

    requires: ['Mdc.model.DeviceLabel'],

    fields: [
        { name: 'mRID', type: 'string'},
        { name: 'serialNumber', type: 'string'},
        { name: 'deviceTypeName', type: 'string'}
    ],

    hasOne: {
        model: 'Mdc.model.DeviceLabel',
        name: 'deviceLabelInfo',
        associationKey: 'deviceLabelInfo',
        getterName: 'getLabel'
    }
});