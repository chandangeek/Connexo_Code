Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,

    proxyFilter: {},

    setProxyFilter: function(filter){
        this.proxyFilter = filter;
        this.load();
        this.fireEvent('setProxyFilter', filter);
    },

    listeners: {
        beforeload: function(store, operation, eOpts) {
            var params = window.btoa(this.proxyFilter);
            this.proxy.extraParams.params = params;
            console.log(params);
        }
    },

    proxy: {
        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json',
            root: 'issueList'
        },
        extraParams: {
            sort: 'dueDate',
            order: 'asc'
        }
    }
});