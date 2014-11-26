Ext.define('Dxp.store.DataSources', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataSource',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/export/dataexporttask/{taskId}/datasources',
        reader: {
            type: 'json',
            root: 'dataSources'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId);
        }
    }
});

