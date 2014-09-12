Ext.define('Mdc.store.Devices',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Device'
    ],
    model: 'Mdc.model.Device',
    storeId: 'Devices',
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});
