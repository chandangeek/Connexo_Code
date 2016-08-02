Ext.define('Mdc.store.DataLoggerSlaveRegisterHistory', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.DataLoggerSlaveRegisterHistory'
    ],

    model: 'Mdc.model.DataLoggerSlaveRegisterHistory',
    storeId: 'DataLoggerSlaveRegisterHistory',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'registerHistory'
        },
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        setUrl: function(mRID, registerId) {
            this.url = '/api/ddr/devices/' + encodeURIComponent(mRID) + '/registers/' + registerId + '/history'
        }
    }

});
