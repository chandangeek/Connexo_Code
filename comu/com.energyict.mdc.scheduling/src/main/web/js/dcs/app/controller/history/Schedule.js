Ext.define('Dcs.controller.history.Schedule', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration_dcs',

    init: function () {
        var me = this;

        crossroads.addRoute('administration_dcs/', function () {
            me.getApplication().getController('Dcs.controller.Administration').showOverview();
        });
        crossroads.addRoute('administration_dcs/schedules', function () {
            me.getApplication().getController('Dcs.controller.Schedule').showDataCollectionSchedules();
        });

        this.callParent(arguments);
    }
});