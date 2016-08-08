Ext.define('Mdc.usagepointmanagement.store.MetrologyConfigurationVersions', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
    proxy: {
        type: 'rest',
        urlTpl: '/api/mtr/usagepoints/{mRID}/history/metrologyconfigurations',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'metrologyConfigurationVersions'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});