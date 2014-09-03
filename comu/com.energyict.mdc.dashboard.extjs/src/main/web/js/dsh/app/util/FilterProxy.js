Ext.define('Dsh.util.FilterProxy', {
    extend: 'Ext.data.proxy.Proxy',
    create: function () {
        this.processData.apply(this, arguments)
    },
    read: function () {
        this.processData.apply(this, arguments)
    },
    update: function () {
        this.processData.apply(this, arguments)
    },
    destroy: function () {
        this.processData.apply(this, arguments)
    },
    processData: function (operation, callback, model) {
        var router = master.app.getController('Uni.controller.history.Router'),
            qObject = model.getData();
        router.getRoute(router.currentRoute).forward(null, qObject)
    }
});
