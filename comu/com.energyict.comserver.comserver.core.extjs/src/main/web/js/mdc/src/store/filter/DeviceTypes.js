Ext.define('Mdc.store.filter.DeviceTypes', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/devicetypes',
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
