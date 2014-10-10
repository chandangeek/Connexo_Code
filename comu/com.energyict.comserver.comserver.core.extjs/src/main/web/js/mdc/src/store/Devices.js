Ext.define('Mdc.store.Devices', {
    extend: 'Uni.data.store.Filterable',

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
