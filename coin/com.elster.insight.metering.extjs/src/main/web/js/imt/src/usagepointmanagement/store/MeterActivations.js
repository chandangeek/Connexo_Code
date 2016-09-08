Ext.define('Imt.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeterActivations',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointMRID}/meteractivations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    },
    setMrid: function (mrid) {
        this.getProxy().url = this.getProxy()
            .urlTpl.replace('{usagePointMRID}', encodeURIComponent(mrid));
    }
});