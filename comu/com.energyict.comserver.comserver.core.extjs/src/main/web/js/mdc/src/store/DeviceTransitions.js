Ext.define('Mdc.store.DeviceTransitions', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceTransition'
    ],
    model: 'Mdc.model.DeviceTransition',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/transitions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'transitions'
        }
    }
});