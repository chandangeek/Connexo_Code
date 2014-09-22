Ext.define('Uni.data.proxy.QueryStringProxy', {
    extend: 'Ext.data.proxy.Proxy',
    alias: 'proxy.querystring',
    root: '',
    router: null,

    requires: [
        'Uni.util.History'
    ],

    writer: {
        type: 'json',
        writeRecordId: false
    },

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
            Model = me.model;

        operation.setStarted();

        if (!_.isUndefined(router.queryParams[me.root])) {
            var data = Ext.decode(router.queryParams[me.root], true);

            if (this.hydrator) {
                var record = Ext.create(Model);
                this.hydrator.hydrate(data, record);

                operation.resultSet = Ext.create('Ext.data.ResultSet', {
                    records: [record],
                    total: 1,
                    loaded: true,
                    success: true
                });
            } else {
                operation.resultSet = me.reader.read(data);
            }

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
            queryParams = {};

        operation.setStarted();

        var data = this.hydrator
            ? this.hydrator.extract(model)
            : this.writer.getRecordData(model);

        //todo: clean empty data!
        model.commit();

        operation.setCompleted();
        operation.setSuccessful();

        queryParams[this.root] = Ext.encode(data);
        router.getRoute().forward(null, queryParams);
    }
});
