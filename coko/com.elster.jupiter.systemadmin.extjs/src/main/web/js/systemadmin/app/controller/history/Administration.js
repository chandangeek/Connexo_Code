Ext.define('Sam.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    init: function () {
        var me = this;

        crossroads.addRoute('administration/licensing/licenses', function () {
            me.getController('Sam.controller.licensing.Licenses').showOverview();
        });
        crossroads.addRoute('administration/licensing/upload', function () {
            me.getController('Sam.controller.licensing.Upload').showOverview();
        });

        this.callParent(arguments);
    }
});
