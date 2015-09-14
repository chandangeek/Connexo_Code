Ext.define('Mdc.store.filter.DeviceTypes', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
