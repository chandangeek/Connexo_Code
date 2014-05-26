Ext.define('Cfg.controller.history.EventType', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'eventtypes',

    init: function () {
        var me = this;

        crossroads.addRoute('eventtypes/', function () {
            me.getApplication().getEventTypeController().showOverview();
        });

        this.callParent(arguments);
    }
});
