Ext.define('Mdc.model.RegisterTypeOnDeviceType', {
    extend: 'Mdc.model.RegisterType',
    requires: [
        'Mdc.model.RegisterType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'obisCode', type: 'string'},
        {name: 'customPropertySet', type: 'auto'},
        'readingType',
        {name: 'name', type: 'string', persist: false, mapping: 'readingType.fullAliasName'}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/registertypes',
        reader: {
            type: 'json'
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});