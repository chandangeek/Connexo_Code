Ext.define('Mdc.store.device.MeterActivations', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MeterActivation'
    ],
    model: 'Mdc.model.MeterActivation',
    pageParam: false,
    startParam: false,
    limitParam: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/history/meteractivations',
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});
