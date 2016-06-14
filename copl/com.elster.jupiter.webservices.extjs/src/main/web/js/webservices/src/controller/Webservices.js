Ext.define('Wss.controller.Webservices', {
    extend: 'Ext.app.Controller',

    views: [
        'Wss.view.Setup'
    ],
    stores: [
        'Wss.store.Endpoints'
    ],
    models: [
        'Wss.model.Endpoint'
    ],

    refs: [

    ],

    init: function () {
        this.control({});
    },

    showWebservicesOverview: function () {
        var me = this,
            view,
            store = me.getStore('Wss.store.Endpoints');

        view = Ext.widget('webservices-setup');
        me.getApplication().fireEvent('changecontentevent', view);
    }
});