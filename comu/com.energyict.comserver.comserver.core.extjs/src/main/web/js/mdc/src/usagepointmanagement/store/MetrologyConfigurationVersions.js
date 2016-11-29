Ext.define('Mdc.usagepointmanagement.store.MetrologyConfigurationVersions', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{usagePointId}/history/metrologyconfigurations',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'metrologyConfigurationVersions'
        }
    }
});