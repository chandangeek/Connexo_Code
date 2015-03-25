Ext.define('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'fromState',
        'toState',
        'privileges',
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
        }
    ]
});
