Ext.define('Isu.controller.DataCollectionOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'workspace.datacollection.Overview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('datacollection-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
