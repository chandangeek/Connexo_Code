Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    showOverview: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Overview');

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add');

        me.getApplication().fireEvent('changecontentevent', view);
    }
});