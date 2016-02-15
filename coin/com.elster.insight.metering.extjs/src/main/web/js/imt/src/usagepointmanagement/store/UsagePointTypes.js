Ext.define('Imt.usagepointmanagement.store.UsagePointTypes', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.UsagePointType',
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/usagepointtype',
        reader: {
            type: 'json',
            root: 'usagePointTypes'
        }
    }
});