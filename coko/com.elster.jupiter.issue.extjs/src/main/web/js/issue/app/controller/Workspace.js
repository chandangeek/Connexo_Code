Ext.define('Isu.controller.Workspace', {
    extend: 'Ext.app.Controller',

    views: [
        'workspace.Overview'
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('workspace-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});