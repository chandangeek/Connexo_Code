Ext.define('Mdc.store.DeviceTopology', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DeviceTopology',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'slaveDevices'
        },
        setUrl: function(mRID) {
            this.url = '/api/ddr/devices/' + encodeURIComponent(mRID) + '/topology/communication'
        }

    }


});
