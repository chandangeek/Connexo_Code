Ext.define('Sam.store.DataPurgeHistoryCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Sam.model.DataPurgeSetting',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/lic/data/history/{historyId}/categories',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (historyId) {
            this.url = this.urlTpl.replace('{historyId}', historyId);
        }
    }
});