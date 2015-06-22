Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionChecks', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionCheck',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/actions/microchecks',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'microChecks'
        },
        setUrl: function (params, fromState, toState) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
            this.extraParams = {fromState: {id: fromState}, toState: {id: toState}};
        }
    }
});