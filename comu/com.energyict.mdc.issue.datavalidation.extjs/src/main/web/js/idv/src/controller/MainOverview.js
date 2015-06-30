Ext.define('Idv.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Idv.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('data-validation-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
