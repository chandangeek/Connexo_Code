Ext.define('Dsh.controller.Communications', {
    extend: 'Dsh.controller.BaseController',

    views: [
        'Dsh.view.Communications',
        'Dsh.view.widget.PreviewCommunication'
    ],

    stores: [
        'Dsh.store.CommunicationTasks'
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
            selector: 'filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: 'dsh-side-filter nested-form'
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
            record = selected[0],
            connTaskData = record.get('connectionTask'),
            connTaskRecord = Ext.create('Dsh.model.ConnectionTask', connTaskData),
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview();

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
});
