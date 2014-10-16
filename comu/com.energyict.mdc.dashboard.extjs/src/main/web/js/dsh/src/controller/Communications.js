Ext.define('Dsh.controller.Communications', {
    extend: 'Dsh.controller.BaseController',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    views: [
        'Dsh.view.Communications',
        'Dsh.view.widget.PreviewCommunication'
    ],

    stores: [
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CommunicationSchedule',
        'Dsh.store.filter.CommunicationTask',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
    ],

    refs: [
        {
            ref: 'communicationPreview',
            selector: '#communicationsdetails #communicationdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#communicationsdetails #connectiondetails'
        },
        {
            ref: 'filterPanel',
            selector: '#communicationsdetails filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#communicationsdetails #filter-form'
        }
    ],

    prefix: '#communicationsdetails',

    init: function () {
        this.control({
            '#communicationsdetails #communicationslist': {
                selectionchange: this.onSelectionChange
            }
        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('communications-details'),
            store = this.getStore('Dsh.store.CommunicationTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.load();
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview(),
            record = selected[0];
        if (record ) {
            preview.loadRecord(record);
            preview.setTitle(record.get('name') + ' on ' + record.get('device').name);
            if (record.getData().connectionTask) {
                var conTask = record.getConnectionTask();
                connPreview.setTitle(conTask.get('connectionMethod').name + ' on ' + conTask.get('device').name);
                connPreview.show();
                connPreview.loadRecord(conTask);
            } else {
                connPreview.hide()
            }
        }
    }
});
