Ext.define('Imt.usagepointmanagement.store.MeterRoles', {
    extend: 'Imt.usagepointmanagement.store.MeterActivations',
    model: 'Imt.usagepointmanagement.model.MeterRole',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/meteractivations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});