Ext.define('Isu.controller.Administration', {
    extend: 'Ext.app.Controller',

    views: [
        'administration.Overview'
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('administration-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});