Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionState',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/states',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceLifeCycleStates'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});