Ext.define('Apr.store.UnservedMessageServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.UnservedMessageService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/unserved',
        reader: {
            type: 'json',
            root: 'subscriberSpecs'
        },

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
