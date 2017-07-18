Ext.define('Mdc.networkvisualiser.store.NetworkNodes', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.graphvisualiser.model.GraphModel'
    ],
    model: 'Uni.graphvisualiser.model.GraphModel',
    storeId: 'networkNodes',

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtg/topology/{deviceId}',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        timeout: 120000,
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});