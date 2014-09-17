Ext.define('Uni.data.proxy.QueryStringProxy', {
    extend: 'Ext.data.proxy.Proxy',
    root: '',
    router: null,

    requires: [
        'Uni.util.History'
    ],

    constructor: function (config) {
        config = config || {};
        this.callParent(arguments);
        if (config.hydrator) {
            this.hydrator = Ext.create(config.hydrator);
        }
        this.router = config.router || Uni.util.History.getRouterController();
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
            Model = me.model,
            id = operation.id;

        operation.setStarted();

        if (!_.isUndefined(router.queryParams[me.root])) {
            var data = Ext.JSON.decode(router.queryParams[me.root]),
                modelData = _.object(_.pluck(data, 'property'), _.pluck(data, 'value')),
                record;

            if (this.hydrator) {
                record = new Model();
                this.hydrator.hydrate(modelData, record);
            } else {
                record = new Model(modelData);
            }

            operation.resultSet = Ext.create('Ext.data.ResultSet', {
                records: [record],
                total: 1,
                loaded: true,
                success: true
            });

            operation.setSuccessful();
        }

        operation.setCompleted();

        if (!operation.wasSuccessful()) {
            me.fireEvent('exception', me, null, operation);
        }

        Ext.callback(callback, scope || me, [operation]);
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
            : model.getData(model);

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
