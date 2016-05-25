Ext.define('Imt.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeterActivations',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointMRID}/meters',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        },
        setUrl: function(usagePointMRID) {
            this.url = this.urlTpl.replace('{usagePointMRID}', usagePointMRID);
        }
    }
});