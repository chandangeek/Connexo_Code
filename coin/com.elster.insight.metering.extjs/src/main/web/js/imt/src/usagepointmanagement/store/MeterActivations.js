Ext.define('Imt.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeterActivations',
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{usagePointMRID}/meteractivations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});