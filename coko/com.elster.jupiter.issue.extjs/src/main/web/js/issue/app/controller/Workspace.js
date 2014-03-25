Ext.define('Isu.controller.Workspace', {
    extend: 'Ext.app.Controller',

    views: [
        'workspace.Overview'
    ],

    init: function () {

    },

    showOverview: function () {
        var widget = Ext.widget('workspace-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});