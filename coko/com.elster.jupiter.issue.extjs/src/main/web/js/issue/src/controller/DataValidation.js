Ext.define('Isu.controller.DataValidation', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.workspace.datavalidation.Overview'
    ],

    showOverview: function () {
        var widget = Ext.widget('datavalidation-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});