Ext.define('Usr.controller.history.Home', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'home',

    init: function () {
        var me = this;

        crossroads.addRoute('home', function () {
            me.getApplication().getController('Usr.controller.Home').showOverview();
        });

        this.callParent(arguments);
    }
});