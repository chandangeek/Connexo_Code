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
        extraParams: {
            filter: '[{"property":"layers","value":["Device types", "Issues/Alarms", "Communication status", "Status of device life cycle"]}]'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        timeout: 240000,
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});