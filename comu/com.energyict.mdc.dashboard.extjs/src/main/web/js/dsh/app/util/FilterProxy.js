Ext.define('Dsh.util.FilterProxy', {
    extend: 'Ext.data.proxy.Proxy',
    create: function () {
        this.setQueryParams.apply(this, arguments)
    },
    read: function () {
        this.getQueryParams.apply(this, arguments)
    },
    update: function () {
        this.setQueryParams.apply(this, arguments)
    },
    destroy: function () {
        this.setQueryParams.apply(this, arguments)
    },
    setQueryParams: function (operation, callback, model) {
        var router = master.app.getController('Uni.controller.history.Router'),
            filterData = model.getData(),
            qObject = {},
            qArray = [];
        delete filterData.id;
        for (key in filterData) {
            var fObj = {};
            fObj.property = key;
            fObj.value = filterData[key];
            if (!Ext.isEmpty(fObj.value)) {
                qArray.push(fObj)
            }
        }
        qObject.filter = qArray;
        router.getRoute(router.currentRoute).forward(null, qObject);
    },
    getQueryParams: function (operation) {
        var router = master.app.getController('Uni.controller.history.Router'),
            filterParams = router.queryParams.filter,
            model = Ext.create('Dsh.model.Filter'),
            filters = Ext.JSON.decode(filterParams);
        Ext.each(filters, function (filter) {
            model.set(filter['property'], filter['value']);
        });
        operation.callback(model)
    }
});
