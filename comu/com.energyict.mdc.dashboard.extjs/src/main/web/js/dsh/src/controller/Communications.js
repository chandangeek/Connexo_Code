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
            selector: '#communicationdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#communicationdetails #connectiondetails'
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
            '#communicationdetails #communicationslist': {
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
        if (record) {
            var connTaskData = record.get('connectionTask'),
                connTaskRecord = Ext.create('Dsh.model.ConnectionTask', connTaskData);
            preview.loadRecord(record);
            preview.setTitle(record.get('name') + ' on ' + record.get('device').name);

            if (connTaskData) {
                connPreview.setTitle(connTaskData.connectionMethod.name + ' on ' + connTaskData.device.name);
                connPreview.show();
                connPreview.loadRecord(connTaskRecord);
            } else {
                connPreview.hide()
            }
        }
    }
});
