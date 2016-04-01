Ext.define('Mdc.store.AvailableDataLoggerSlaves', {
    extend: 'Uni.data.store.Filterable',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'AvailableDataLoggerSlaves',

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});
