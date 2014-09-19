Ext.define('Uni.data.store.Filterable', {
    extend: 'Ext.data.Store',

    constructor: function(config) {
        var me = this;
        this.callParent(arguments);
        var router = this.router = config.router || Uni.util.History.getRouterController();

        router.on('routematch', function() {
            if (router.filter) {
                me.clearFilter(true);
                me.addFilter(router.filter.getFilterData(), false);
            }
        });
    }
});

