Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.ConnectionDetails'
    ],
    stores: [
        'Dsh.store.ConnectionsStore'
    ],
    views: [
        'Dsh.view.Connections'
    ],
    refs: [],
    init: function () {
        this.control({

        });
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('connections-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
