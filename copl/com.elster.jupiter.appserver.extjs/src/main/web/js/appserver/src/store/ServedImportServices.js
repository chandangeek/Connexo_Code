Ext.define('Apr.store.ServedImportServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ServedImportService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/servedimport',
        reader: {
            type: 'json',
            root: 'importServices'
        },

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
