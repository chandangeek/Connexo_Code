Ext.define('Mtr.controller.Error', {
    extend: 'Ext.app.Controller',
    stores: [
    ],
    models: [
    ],
    views: [
        'error.Page',
        'error.Http404'
    ],
    refs: [
    ],

    init: function () {
        // TODO
    },

    browse: function () {
        var widget = Ext.widget('errorPage');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showHttp404: function () {
        var widget = Ext.widget('errorHttp404');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});