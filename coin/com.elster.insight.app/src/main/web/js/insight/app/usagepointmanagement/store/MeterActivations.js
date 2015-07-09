Ext.define('InsightApp.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'InsightApp.usagepointmanagement.model.MeterActivations',
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