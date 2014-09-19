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
            selector: '#connectiondetails'
        },
        {
            ref: 'filterPanel',
            selector: '#dshcommunicationsfilterpanel'
        },
        {
            ref: 'sideFilterForm',
            selector: 'dsh-comm-side-filter nested-form'
        }
    ],

    init: function () {
        this.control({
            '#communicationslist': {
                selectionchange: this.onSelectionChange
            }
        });

        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('communications-details'),
            router = this.getController('Uni.controller.history.Router'),
            store = this.getStore('Dsh.store.CommunicationTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();

//        store.setFilterModel(router.filter);
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
