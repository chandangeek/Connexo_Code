Ext.define('Apr.store.UnservedImportServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ImportService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/unservedimport',
        reader: {
            type: 'json',
            root: 'importServices'
        },

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
