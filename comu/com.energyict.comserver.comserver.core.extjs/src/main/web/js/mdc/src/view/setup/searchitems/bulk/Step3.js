/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'searchitems-bulk-step3',
    name: 'selectActionItems',
    ui: 'large',

    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.searchitems.bulk.SchedulesSelectionGrid'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'MDC', 'Step 3: Action details'),

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
                    hidden: true
                }
            ]
        },
        {
            xtype: 'preview-container',
            itemId: 'select-schedules-panel',
            selectByDefault: false,
            grid: {
                xtype: 'schedules-selection-grid',
                itemId: 'schedulesgrid'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('setup.searchitems.bulk.Step3.NoItemsFoundPanel.title', 'MDC', 'No shared communication schedules found'),
                reasons: [
                    Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No shared communication schedules have been created yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('communicationSchedule.add', 'MDC', 'Add shared communication schedule'),
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
                margin: '0 0 5 0',
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
                                        result += Ext.String.htmlEncode(item.name) + '<br>';
                                    });
                                    return result;
                                }
                            }
                        ]
                    }
                ],
                emptyText: '<h3>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No shared communication schedule selected') + '</h3><p>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a shared communication schedule to see its details') + '</p>'
            }
        },
        {
            xtype: 'form',
            itemId: 'change-device-configuration',
            hidden: true,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('searchItems.bulk.currentDeviceConfig', 'MDC', 'Current device configuration'),
                    itemId: 'current-device-config-selection',
                    name: 'fromconfig'
                },
                {
                    xtype: 'combobox',
                    name: 'toconfig',
                    itemId: 'new-device-config-selection',
                    fieldLabel: Uni.I18n.translate('searchItems.bulk.newDeviceConfig', 'MDC', 'New device configuration'),
                    emptyText: Uni.I18n.translate('general.selectDeviceConfiguration', 'MDC', 'Select a device configuration...'),
                    required: true,
                    width: 200+256,
                    allowBlank: false,
                    queryMode: 'local',
                    store: 'Mdc.store.BulkDeviceConfigurations',
                    displayField: 'name',
                    valueField: 'id',
                    listConfig: {
                        loadMask: false
                    }
                },
                {
                    xtype: 'displayfield',
                    itemId: 'no-device-configuration',
                    fieldLabel: Uni.I18n.translate('searchItems.bulk.newDeviceConfig', 'MDC', 'New device configuration'),
                    value: Uni.I18n.translate('general.noOtherActiveNonDataLoggerDeviceConfigurationsDefined', 'MDC', 'No other active, non-data logger device configurations defined.'),
                    fieldStyle: 'color: #eb5642',
                    required: true,
                    hidden: true
                }
            ]
        },
        {
            xtype: 'container',
            itemId: 'stepSelectionError',
            hidden: true,
            html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1communicationschedule', 'MDC', 'Select at least 1 shared communication schedule') + '</span>'
        }
    ]
});