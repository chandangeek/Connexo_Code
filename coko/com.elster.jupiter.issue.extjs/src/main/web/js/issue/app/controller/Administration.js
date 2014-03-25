Ext.define('Isu.controller.Administration', {
    extend: 'Ext.app.Controller',

    views: [
        'administration.Overview'
    ],

    init: function () {

    },

    showOverview: function () {
        var widget = Ext.widget('administration-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});