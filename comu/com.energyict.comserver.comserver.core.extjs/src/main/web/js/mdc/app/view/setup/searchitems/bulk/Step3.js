Ext.define('Mdc.view.setup.searchitems.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step3',
    name: 'selectSchedules',
    ui: 'large',

    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormErrorMessage'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'MDC', 'Bulk action - step 3 of 5: Action details'),

    items: [
        {
            xtype: 'panel',
            ui: 'medium',
            title: '',
            itemId: 'searchitemsbulkactiontitle',
            layout: {
                type: 'vbox',
                align: 'left'
            },
            width: '100%',
            items: [
                {
                    itemId: 'step3-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('searchItems.bulk.comSchedulesError', 'MDC', 'It is required to select one or more devices to go to the next step')
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
                            boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.allSchedules', 'MDC', 'All communication schedules') + '</b>',
                            name: 'scheduleRange',
                            inputValue: 'ALL'
                        },
                        {
                            itemId: 'selectedSchedules',
                            boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.selectedSchedules', 'MDC', 'Selected communication schedules') + '</b></br>' +
                                '</b><span style="color: grey;">' + Uni.I18n.translate('searchItems.bulk.selectedScheduleInTable', 'MDC', 'Select communication schedules in table') +
                                '</span>',
                            name: 'scheduleRange',
                            inputValue: 'SELECTED',
                            checked: true
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
                            html: '<span style="color: grey;">'
                                + Uni.I18n.translate('searchItems.bulk.noScheduleSelected', 'MDC', 'No schedule selected')
                                + '</span>'
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
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'grid',
                itemId: 'schedulesgrid',
                store: 'Mdc.store.CommunicationSchedulesWithoutPaging',
                height: 355,
                overflowY: 'auto',
                selType: 'checkboxmodel',
                selModel: {
                    checkOnly: true,
                    enableKeyNav: false,
                    showHeaderCheckbox: false
                },
                columns: {
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
                            renderer: function (value) {
                                return Mdc.util.ScheduleToStringConverter.convert(value);
                            },
                            flex: 0.4
                        },
                        {
                            header: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                            dataIndex: 'plannedDate',
                            renderer: function (value) {
                                return Uni.I18n.formatDate('general.dateFormat.long', value, 'MDC', 'M d Y H:i A');
                            },
                            flex: 0.4
                        }
                    ]
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('setup.searchitems.bulk.Step3.NoItemsFoundPanel.title', 'MDC', 'No communication schedules found'),
                reasons: [
                    Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No communication schedules have been created yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('communicationschedule.add', 'MDC', 'Add communication schedule'),
                        action: 'createCommunicationSchedule',
                        itemId: 'createCommunicationSchedule'
                    }
                ]
            },
            previewComponent: {
                xtype: 'panel',
                frame: true,
                itemId: 'communicationschedulepreview',
                hidden: true,
                requires: [
                    'Mdc.model.DeviceType'
                ],
                items: [
                    {
                        xtype: 'form',
                        itemId: 'communicationschedulepreviewporm',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
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
                                xtype: 'displayfield',
                                name: 'comTaskUsages',
                                fieldLabel: Uni.I18n.translate('communicationschedule.communicationTasks', 'MDC', 'Communication task(s)'),
                                renderer: function (value) {
                                    var result = '';
                                    Ext.isArray(value) && Ext.Array.each(value, function (item) {
                                        result += item.name + '<br>';
                                    });
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'schedulingStatus',
                                fieldLabel: Uni.I18n.translate('communicationschedule.status', 'MDC', 'Status'),
                                readOnly: true
                            }
                        ]
                    }
                ],
                emptyText: '<h3>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No communication schedule selected') + '</h3><p>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a communication schedule to see its details') + '</p>'
            },

            initGridListeners: function () {}
        }
    ]
});