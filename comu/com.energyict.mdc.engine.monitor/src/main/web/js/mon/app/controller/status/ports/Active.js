Ext.define('CSMonitor.controller.status.ports.Active', {
    extend: 'Ext.app.Controller',

    stores: ['status.ports.Active'],
    models: ['status.Port'],
    views: ['status.ports.Active'],

    refs: [
        {
            ref: 'comViewPanel',
            selector: 'communication'
        }
    ],

    init: function() {
        this.control({
            'activePorts': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            },
            'activePorts': {
                itemclick: this.onItemClicked
            }
        });
    },

    refreshData: function() {
        this.getStatusPortsActiveStore().load();
    },

    onItemClicked: function(self, record, htmlElement, itemIndex, event) {
        var clickedComPortId = record.data.id;
        this.getComViewPanel().openWindow('#logging/comm/portid=' + clickedComPortId);
    }
});