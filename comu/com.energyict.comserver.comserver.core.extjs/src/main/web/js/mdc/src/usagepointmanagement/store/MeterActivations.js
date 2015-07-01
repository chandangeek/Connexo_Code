Ext.define('Mdc.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.MeterActivations',
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{mRID}/meteractivations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});