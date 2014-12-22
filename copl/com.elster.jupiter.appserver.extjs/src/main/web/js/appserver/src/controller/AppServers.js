Ext.define('Apr.controller.AppServers', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.appservers.Setup'
    ],
    stores: [
        'Apr.store.AppServers'
    ],
    models: [
        'Apr.model.AppServer'
    ],

    showAppServers: function () {
        var me = this,
            view = Ext.widget('appservers-setup');

        me.getApplication().fireEvent('changecontentevent', view);
    }
});