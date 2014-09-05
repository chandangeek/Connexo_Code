Ext.define('Uni.data.proxy.QueryStringProxy', {
    extend: 'Ext.data.proxy.Proxy',
    root: 'filter',

    router: 'null',

    constructor: function(config) {
        config = config || {};
        this.callParent(arguments);
        this.router = config.router || master.app.getController('Uni.controller.history.Router');
    },

    create: function () {
        this.setQueryParams.apply(this, arguments);
    },

    update: function () {
        this.setQueryParams.apply(this, arguments);
    },

    read: function (operation, callback, scope) {
        var me = this,
            router = me.router,
            model = new me.model()
        ;

        operation.setStarted();

        if (!_.isUndefined(router.queryParams[me.root])) {
            var data = Ext.JSON.decode(router.queryParams[me.root]);
            Ext.each(data, function (item) {
                model.set(item['property'], item['value']);
            });
        }

        operation.setCompleted();
        operation.setSuccessful();

        operation.resultSet = Ext.create('Ext.data.ResultSet', {
            records: [model],
            total  : 1,
            loaded : true
        });

        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }
    },

    /**
     * removes params
     */
    destroy: function () {
        var router = master.app.getController('Uni.controller.history.Router');
        delete router.queryParams[this.root];

        //should redirect be performed via proxy? How it will work with another models, like sorting?
        router.getRoute().forward();
    },

    setQueryParams: function (operation, callback, model) {
        var router = master.app.getController('Uni.controller.history.Router'),
            queryParams = {},
            filter = []
        ;

        operation.setStarted();

        model.fields.each(function(item){
            if (!Ext.isEmpty(model.get(item.name))) {
                filter.push({
                    property: item.name,
                    value: model.get(item.name)
                });
            }
        });
        model.commit();

        operation.setCompleted();
        operation.setSuccessful();

        queryParams[this.root] = filter;
        router.getRoute().forward(null, queryParams);
    }
});
