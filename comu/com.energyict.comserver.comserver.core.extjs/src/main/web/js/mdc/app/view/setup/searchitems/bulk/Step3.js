Ext.define('Mdc.view.setup.searchitems.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step3',
    name: 'selectSchedules',
    ui: 'large',

    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'MDC', 'Bulk action - step 3 of 5: Action details'),

    tbar: {
        xtype: 'panel',
        ui: 'medium',
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },

    items: {
        xtype: 'preview-container',
        grid: {
            xtype: 'grid',
            itemId: 'schedulesgrid',
            store: 'Mdc.store.CommunicationSchedules',
            scroll: false,
            intervalStore: null,

            viewConfig: {
                style: {
                    overflow: 'auto',
                    overflowX: 'hidden'
                }
            },
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                enableKeyNav: false,
                showHeaderCheckbox: false
            },
            tbar: {
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                width: '100%',
                items: [
                    {
                        name: 'step3-errors',
                        layout: 'hbox',
                        hidden: true,
                        items: [
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message'
                            }
                        ]
                    },
                    {
                        itemId: 'shceduleSelectionRange',
                        xtype: 'radiogroup',
                        name: 'selectionMode',
                        columns: 1,
                        vertical: true,
                        submitValue: false,
                        defaults: {
                            padding: '0 0 30 0'
                        },
                        items: [
                            {
                                itemId: 'allSchedules',
                                boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.allShcedules', 'MDC', 'All communication schedules') + '</b>',
                                name: 'scheduleRange',
                                inputValue: 'ALL',
                                checked: true
                            },
                            {
                                itemId: 'selectedSchedules',
                                boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.selectedDevices', 'MDC', 'Selected schedule') + '</b></br>' +
                                    '</b><span style="color: grey;">' + Uni.I18n.translate('searchItems.bulk.selectedScheduleInTable', 'MDC', 'Select communication schedules in table') +
                                    '</span>',
                                name: 'scheduleRange',
                                inputValue: 'SELECTED'
                            }
                        ]
                    },
                    {
                        itemId: 'selectedschedules',
                        xtype: 'container',
                        name: 'schedules-devices',
                        width: '100%',
                        layout: {
                            type: 'hbox',
                            align: 'middle'
                        },
                        items: [
                            {
                                itemId: 'schedule-qty-txt',
                                xtype: 'container',
                                name: 'schedule-qty-txt',
                                html: '<span style="color: grey;">' + Uni.I18n.translate('searchItems.bulk.noScheduleSelected', 'MDC', 'No schedule selected') +
                                    '</span>'
                            },
                            {
                                itemId: 'uncheck-all',
                                xtype: 'button',
                                name: 'uncheck-all-btn',
                                text: 'Uncheck all',
                                margin: '0 0 0 16'
                            }
                        ]
                    }
                ]
            },
            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
                    {
                        header: Uni.I18n.translate('communicationschedule.status', 'MDC', 'Status'),
                        dataIndex: 'schedulingStatus',
                        flex: 0.1
                    },
                    {
                        header: Uni.I18n.translate('communicationschedule.name', 'MDC', 'Name'),
                        dataIndex: 'name',
                        flex: 0.4
                    },
                    {
                        header: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                        dataIndex: 'temporalExpression',
                        renderer: function (value, metadata) {
                            switch (value.every.timeUnit) {
                                case 'months':
                                    return Uni.I18n.translate('general.monthly', 'MDC', 'Monthly');
                                case 'weeks':
                                    return Uni.I18n.translate('general.weekly', 'MDC', 'Weekly');
                                case 'days':
                                    return Uni.I18n.translate('general.daily', 'MDC', 'Daily');
                                case 'hours':
                                    return Uni.I18n.translate('general.hourly', 'MDC', 'Hourly');
                                case 'minutes':
                                    return Uni.I18n.translate('general.everyFewMinutes', 'MDC', 'Every few minutes');
                            }
                            return value.every.timeUnit;
                        },
                        flex: 0.4
                    },
                    {
                        header: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                        dataIndex: 'plannedDate',
                        renderer: function (value) {
                            if (value !== null) {
                                return new Date(value).toLocaleString();
                            } else {
                                return '';
                            }
                        },
                        flex: 0.4
                    }
                ]
            }
        },
        emptyComponent: {
            xtype: 'no-items-found-panel',
            title: Uni.I18n.translate('setup.searchitems.bulk.Step3.NoItemsFoundPanel.title', 'MDC', 'No communication schedules found')
        },
        previewComponent: {
            xtype: 'panel',
            frame: true,
            header: {
                title: ''
            },
            title: '',
            itemId: 'communicationschedulepreview',
            requires: [
                'Mdc.model.DeviceType'
            ],
            items: [
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'communicationschedulepreviewporm',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'column'
                            },
                            items: [
                                {
                                    xtype: 'container',
                                    columnWidth: 0.5,
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    defaults: {
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            name: 'temporalExpression',
                                            fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                                            renderer: function (value) {
                                                return Mdc.util.ScheduleToStringConverter.convert(value);
                                            }
                                        },
                                        {
                                            xtype: 'fieldcontainer',
                                            name: 'communicationTasks',
                                            fieldLabel: Uni.I18n.translate('communicationschedule.communicationTasks', 'MDC', 'Communication tasks'),
                                            items: [
                                                {
                                                    xtype: 'container',
                                                    itemId: 'comtaskpreviewcontainer',
                                                    items: [
                                                    ]
                                                }
                                            ]
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'schedulingStatus',
                                            fieldLabel: Uni.I18n.translate('communicationschedule.status', 'MDC', 'Status'),
                                            readOnly: true
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ],
            emptyText: '<h3>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No communication schedule selected') + '</h3><p>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a communication schedule to see its details') + '</p>'
        },

        initGridListeners: function () {
        }
    }

});