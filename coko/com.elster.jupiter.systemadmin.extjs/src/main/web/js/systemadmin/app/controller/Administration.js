Ext.define('Sam.controller.Administration', {
    extend: 'Ext.app.Controller',

    views: [
        'administration.Overview'
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('sam-administration-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
