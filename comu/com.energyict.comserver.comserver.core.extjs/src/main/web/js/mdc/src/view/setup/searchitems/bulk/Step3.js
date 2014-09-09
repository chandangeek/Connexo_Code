Ext.define('Mdc.view.setup.searchitems.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'searchitems-bulk-step3',
    name: 'selectSchedules',
    ui: 'large',

    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.searchitems.bulk.SchedulesSelectionGrid'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'MDC', 'Bulk action - Step 3 of 5: Action details'),

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
            style: {
                padding: '0 0 0 3px'
            },
            width: '100%',
            items: [
                {
                    itemId: 'step3-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('searchItems.bulk.comSchedulesError', 'MDC', 'It is required to select one or more devices to go to the next step')
                }
            ]
        },
        {
            xtype: 'preview-container',
            selectByDefault: false,
            grid: {
                xtype: 'schedules-selection-grid',
                itemId: 'schedulesgrid'
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
                            }
                        ]
                    }
                ],
                emptyText: '<h3>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No communication schedule selected') + '</h3><p>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a communication schedule to see its details') + '</p>'
            }
        }
    ]
});