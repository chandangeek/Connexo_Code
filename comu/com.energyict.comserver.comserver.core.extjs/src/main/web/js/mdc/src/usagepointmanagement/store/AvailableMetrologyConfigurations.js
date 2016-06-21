Ext.define('Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        urlTpl: '/api/mtr/usagepoints/{usagePointId}/availablemetrologyconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{usagePointId}', encodeURIComponent(mRID));
        }
    }
});