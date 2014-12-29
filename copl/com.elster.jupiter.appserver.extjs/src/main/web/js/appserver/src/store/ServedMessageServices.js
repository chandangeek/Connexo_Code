Ext.define('Apr.store.ServedMessageServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ServedMessageService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}',
        reader: {
            type: 'json',
            root: 'executionSpecs'
        },

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
