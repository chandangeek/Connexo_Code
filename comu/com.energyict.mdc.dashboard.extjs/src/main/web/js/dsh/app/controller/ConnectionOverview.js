Ext.define('Dsh.controller.ConnectionOverview', {
    views: [
        'Dsh.view.Main'
    ],
    models: [
        'Dsh.model.Connections'
    ],
    extend: 'Ext.app.Controller',
    showOverview: function () {
        var me = this;
            screen = Ext.widget('app-main');
        me.getApplication().fireEvent('changecontentevent', screen);
    }
});