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
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
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
            selector: '#dshconnectionssidefilter nested-form'
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
            '#dshconnectionsfilterpanel': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearFilter
            },
            '#dshconnectionssidefilter nested-form side-filter-combo': {
                change: this.onFilterChange
            }
        });
        this.callParent(arguments);
    },

    onFilterChange: function (combo) {
        this.getFilterPanel().setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
    },

    showOverview: function () {
        var widget = Ext.widget('connections-details');
        this.getApplication().fireEvent('changecontentevent', widget);
        var router = this.getController('Uni.controller.history.Router');

        this.getSideFilterForm().loadRecord(router.filter);

        if (router.filter.startedBetween) {
            if (router.filter.startedBetween.get('from')) {
                this.getFilterPanel().setFilter('startedBetween.from', 'Started between from', Ext.util.Format.date(router.filter.startedBetween.get('from'), 'd/m/Y H:i'));
            }
            if (router.filter.startedBetween.get('to')) {
                this.getFilterPanel().setFilter('startedBetween.to', 'Started between to', Ext.util.Format.date(router.filter.startedBetween.get('to'), 'd/m/Y H:i'));
            }
        }

        if (router.filter.finishedBetween) {
            if (router.filter.finishedBetween.get('from')) {
                this.getFilterPanel().setFilter('finishedBetween.from', 'Finished between from', Ext.util.Format.date(router.filter.finishedBetween.get('from'), 'd/m/Y H:i'));
            }
            if (router.filter.finishedBetween.get('to')) {
                this.getFilterPanel().setFilter('finishedBetween.to', 'Finished between to', Ext.util.Format.date(router.filter.finishedBetween.get('to'), 'd/m/Y H:i'));
            }
        }

        var store = this.getStore('Dsh.store.ConnectionTasks');
        store.setFilterModel(router.filter);
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
            record = selected[0];
        if (record) {
            var commTasksData = record.get('communicationTasks').communicationsTasks,
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
        }
    },

    applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    removeFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;
        switch (key) {
            case 'startedBetween.from':
                record.startedBetween.set('from', null);
                break;
            case 'startedBetween.to':
                record.startedBetween.set('to', null);
                break;
            case 'finishedBetween.from':
                record.finishedBetween.set('from', null);
                break;
            case 'finishedBetween.to':
                record.finishedBetween.set('to', null);
                break;
            default:
                record.set(key, null);
        }
        record.save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    }
});
