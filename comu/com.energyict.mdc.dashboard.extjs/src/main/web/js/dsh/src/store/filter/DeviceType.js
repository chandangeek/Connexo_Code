Ext.define('Dsh.store.filter.DeviceType', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/devicetypes',
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});

