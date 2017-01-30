Ext.define('Imt.usagepointmanagement.store.MeterRoles', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeterRole',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/meterroles',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});