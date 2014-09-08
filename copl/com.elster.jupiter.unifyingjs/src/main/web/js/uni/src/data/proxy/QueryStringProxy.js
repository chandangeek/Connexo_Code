Ext.define('Uni.data.proxy.QueryStringProxy', {
    extend: 'Ext.data.proxy.Proxy',
    root: 'filter',
    router: null,

    constructor: function (config) {
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
            router = me.router
        ;

        operation.setStarted();

        if (!_.isUndefined(router.queryParams[me.root])) {
            var data = Ext.JSON.decode(router.queryParams[me.root]);
            var modelData = _.object(_.pluck(data, 'property'), _.pluck(data, 'value'));
            operation.resultSet = this.reader.read(modelData);
        }

        operation.setCompleted();
        operation.setSuccessful();

        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }
    },

    /**
     * removes params
     */
    destroy: function () {
        var router = this.router;
        delete router.queryParams[this.root];

        //should redirect be performed via proxy? How it will work with another models, like sorting?
        router.getRoute().forward();
    },

    setQueryParams: function (operation, callback, model) {
        var router = this.router,
            queryParams = {},
            filter = []
            ;

        operation.setStarted();

        var data = this.hydrator
                ? this.hydrator.extract(model)
                : model.getData(model)
            ;

        _.map(data, function (item, key) {
            if (!Ext.isEmpty(item)) {
                filter.push({
                    property: key,
                    value: item
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
