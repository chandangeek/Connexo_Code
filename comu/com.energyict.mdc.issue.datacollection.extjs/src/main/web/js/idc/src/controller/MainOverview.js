Ext.define('Idc.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Idc.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('data-collection-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
