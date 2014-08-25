Ext.define('Dsh.controller.Communications', {
    extend: 'Ext.app.Controller',
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
        }
    ],

    init: function () {
        this.control({
            '#communicationslist': {
                selectionchange: this.onSelectionChange
            }
        });
        this.getStore('Dsh.store.CommunicationTasks').load();
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('communications-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();
        preview.loadRecord(record);
        preview.setTitle(record.get('name'));
    }
});
