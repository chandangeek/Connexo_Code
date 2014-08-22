Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask'
    ],
    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.CommunicationTasks'
    ],
    views: [
        'Dsh.view.Connections',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
    ],
    refs: [
        {
            ref: 'connectionsList',
            selector: '#connectionsdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#connectionpreview'
        },
        {
            ref: 'communicationList',
            selector: '#communicationsdetails'
        },
        {
            ref: 'communicationContainer',
            selector: '#communicationcontainer'
        },
        {
            ref: 'communicationPreview',
            selector: '#communicationpreview'
        },
        {
            ref: 'commTasksTitle',
            selector: '#comtaskstitlepanel'
        }
    ],
    init: function () {
        this.control({
            '#connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            '#communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('connections-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();
        record.data.devConfig = me.getConnectionsList().getSelectionModel().getSelection()[0].get('devConfig');
        preview.loadRecord(record);
        preview.setTitle(record.get('name'))
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getConnectionPreview(),
            commTasksData = record.get('communicationTasks').communicationTasks,
            commTasks = Ext.create('Ext.data.Store', {model: 'Dsh.model.CommunicationTask',data: commTasksData});
        preview.loadRecord(record);
        preview.setTitle(record.get('name'));
        me.getCommunicationContainer().removeAll(true);
        me.getCommunicationContainer().add({
            xtype: 'preview-container',
            grid: {
                xtype: 'communications-list',
                itemId: 'communicationsdetails',
                store: commTasks
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('communication.widget.details.empty.title', 'DSH', 'No communications foundю'),
                reasons: [
                    Uni.I18n.translate('communication.widget.details.empty.list.item1', 'DSH', 'No communications in the system.'),
                    Uni.I18n.translate('communication.widget.details.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                ]
            },
            previewComponent: {
                xtype: 'preview_communication',
                itemId: 'communicationpreview'
            }
        });
        me.getCommTasksTitle().setTitle(Uni.I18n.translate('communication.widget.details.commTasksOf', 'DSH', 'Communication tasks of'));
        me.getCommunicationList().getSelectionModel().select(0);
    }
});
