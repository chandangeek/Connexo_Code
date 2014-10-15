Ext.define('Dsh.view.widget.CommunicationSideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-comm-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.util.FilterHydrator'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            hydrator: 'Dsh.util.FilterHydrator',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'side-filter-combo',
                labelAlign: 'top'
            },
            items: [
                {
                    itemId: 'device-group',
                    name: 'deviceGroups',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceGroup'
                },
                {
                    itemId: 'current-state',
                    name: 'currentStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'taskStatus',
                    store: 'Dsh.store.filter.CurrentState'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResults',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    displayField: 'localizedValue',
                    valueField: 'completionCode',
                    store: 'Dsh.store.filter.CompletionCodes'
                },
                {
                    itemId: 'communication-task',
                    name: 'comTasks',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.commTask', 'DSH', 'Communication task'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommunicationTask'
                },
                {
                    itemId: 'communication-schedule',
                    name: 'comSchedules',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.communicationSchedule', 'DSH', 'Communication schedule'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommunicationSchedule'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceType'
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'started-between',
                    name: 'startedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.startedBetween', 'DSH', 'Started between')
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'finished-between',
                    name: 'finishedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished successfully between')
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DSH', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});

