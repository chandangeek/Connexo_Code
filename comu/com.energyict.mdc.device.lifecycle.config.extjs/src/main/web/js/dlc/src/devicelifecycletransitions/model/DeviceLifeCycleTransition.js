Ext.define('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'fromState',
        'toState',
        'privileges',
        'triggeredBy',
        'microActions',
        'microChecks',
        {
            name: 'fromState_name',
            persist: false,
            mapping: function (data) {
                return data.fromState.name;
            }
        },
        {
            name: 'toState_name',
            persist: false,
            mapping: function (data) {
                return data.toState.name;
            }
        },
        {
            name: 'triggeredBy_name',
            persist: false,
            mapping: function (data) {
                return data.triggeredBy.name;
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/actions',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});
