Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
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
        },
        {
            ref: 'filterPanel',
            selector: '#dshconnectionsfilterpanel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#dshconnectionssidefilter form'
        }
    ],
    init: function () {
        this.control({
            '#connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            '#communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            '#dshconnectionssidefilter button[action=applyfilter]': {
                click: this.applyFilter
            },
            '#dshconnectionssidefilter': {
                afterrender: this.loadFilterValues
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        var me = this,
            widget = Ext.widget('connections-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    loadFilterValues: function () {
        var me = this;
        Ext.defer(function () {
                Dsh.model.Filter.load(0, {
                    callback: function (record) {
                        me.getSideFilterForm().loadRecord(record)
                    }
                });
            }, 3500)
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();
        record.data.devConfig = {
            config: record.data.deviceConfiguration,
            devType: record.data.deviceType
        };
        record.data.title = record.data.name + ' on ' + record.data.device.name;
        preview.setTitle(record.data.title);
        preview.loadRecord(record);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getConnectionPreview(),
            commTasksData = record.get('communicationTasks').communicationTasks,
            commTasks = Ext.create('Ext.data.Store', {model: 'Dsh.model.CommunicationTask', data: commTasksData});
        preview.loadRecord(record);
        preview.setTitle(record.get('title'));
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
        me.getCommTasksTitle().setTitle(Uni.I18n.translate('communication.widget.details.commTasksOf', 'DSH', 'Communication tasks of') + ' ' + record.get('title'));
        me.getCommunicationList().getSelectionModel().select(0);
    },

    applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        var model = this.getSideFilterForm().getRecord();
        model.save()
    }
});
