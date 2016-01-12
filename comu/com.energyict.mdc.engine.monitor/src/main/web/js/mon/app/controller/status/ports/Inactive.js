Ext.define('CSMonitor.controller.status.ports.Inactive', {
    extend: 'Ext.app.Controller',

    stores: ['status.ports.Inactive'],
    models: ['status.Port'],
    views: ['status.ports.Inactive'],

    init: function() {
        this.control({
            'inactivePorts': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusPortsInactiveStore().load();
    }

});