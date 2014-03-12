Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,

    extraParams: {
        "sort": {},
        "group": {},
        "filter": {}
    },

    setProxyFilter: function(filter) {
        this.extraParams.filter = filter;
        this.load();
        this.fireEvent('updateProxyFilter', this.extraParams.filter);
    },

    removeProxyFilter: function(key) {
        if (!key) {
            this.extraParams.filter = {};
        } else {
            if (!_.isUndefined(this.extraParams.filter[key])){
                delete this.extraParams.filter[key];
            }
        }
        this.load();
        this.fireEvent('updateProxyFilter', this.extraParams.filter);
    },

    listeners: {
        "beforeLoad": function() {
            this.proxy.extraParams.params = window.btoa(Ext.encode(this.extraParams));
            console.log(this.proxy.extraParams.params);
            console.log(this.extraParams);
        }
    }
});