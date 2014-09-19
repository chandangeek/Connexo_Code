Ext.define('Dsh.controller.Connections', {
    extend: 'Dsh.controller.BaseController',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    stores: [
        'Dsh.store.ConnectionTasks'
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
            selector: 'filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: 'dsh-side-filter nested-form'
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
        var widget = Ext.widget('connections-details'),
            router = this.getController('Uni.controller.history.Router'),
            store = this.getStore('Dsh.store.ConnectionTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.setFilterModel(router.filter);
        store.load();
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
            preview = me.getConnectionPreview(),
            commPreview = me.getCommunicationContainer(),
            commPanel = me.getCommTasksTitle(),
            record = selected[0];
        if (!_.isUndefined(record)) {
            var commTasksData = record.get('communicationTasks').communicationsTasks;
            preview.loadRecord(record);
            preview.setTitle(record.get('title'));
            preview.show();
            if (!_.isEmpty(commTasksData)) {
                commPreview.removeAll(true);
                var commTasks = Ext.create('Ext.data.Store', {model: 'Dsh.model.CommunicationTask', data: commTasksData});
                commPreview.add({
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
                commPanel.setTitle(Uni.I18n.translate('communication.widget.details.commTasksOf', 'DSH', 'Communication tasks of') + ' ' + record.get('title'));
                commPanel.show();
                me.getCommunicationList().getSelectionModel().select(0);
            } else {
                commPanel.hide()
            }
        } else {
            preview.hide();
            commPanel.hide();
        }
    }
});
