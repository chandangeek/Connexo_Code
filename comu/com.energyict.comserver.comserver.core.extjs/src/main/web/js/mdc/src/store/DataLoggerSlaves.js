Ext.define('Mdc.store.DataLoggerSlaves', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DataLoggerSlaves',
    sorters: [{
        property: 'mRID',
        direction: 'ASC'
    }],

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'dataLoggerSlaveDevices'
        },
        setUrl: function(mRID) {
            this.url = '/api/ddr/devices/' + encodeURIComponent(mRID) + '/dataloggerslaves'
        }

    }


});
