Ext.define('Imt.usagepointsetup.model.UsagePoint', {
    extend: 'Imt.usagepointmanagement.model.UsagePoint',
    idProperty: undefined,
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{0}/meters',
        url: '',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});