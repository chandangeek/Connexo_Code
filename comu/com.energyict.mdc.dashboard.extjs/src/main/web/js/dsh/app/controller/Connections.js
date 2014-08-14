Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.ConnectionDetails'
    ],
    stores: [
        'Dsh.store.ConnectionsStore',
        'Dsh.store.ConnectionTasks'
    ],
    views: [
        'Dsh.view.Connections',
        'Dsh.view.widget.PreviewConnection'
    ],
    refs: [
        {
            ref: 'connectionPreview',
            selector: '#connectionpreview'
        }
    ],
    init: function () {
        this.control({
            '#connectionsdetails': {
                selectionchange: this.onSelectionChange
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('connections-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0];
        console.log(record);
        this.getConnectionPreview().loadRecord(record);
        this.getConnectionPreview().setTitle(record.get('title'));
    }
});
