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
        'Mdc.view.setup.searchitems.bulk.SchedulesSelectionGrid',
        'Bpm.startprocess.view.StartProcess',
        'Mdc.view.setup.searchitems.bulk.ZoneSelectionPanel',
        'Isu.store.IssueDevices',
        'Isu.store.IssueReasons',
        'Isu.store.DueinTypes',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.UserList',
        'Isu.view.issues.AddManuallyRuleItem',
        'Mdc.view.setup.searchitems.bulk.ZoneSelectionPanel',
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'MDC', 'Step 3: Action details'),

    initComponent: function(){
        var me = this;
        me.items = [
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
            },
            {
                xtype: 'bpm-start-processes-panel',
                itemId: 'bulk-start-processes-panel',
                hideButtons: true,
                hideTitle: true,
                doNotLoadStore: true,
                hidden: true,
                properties: {
                    activeProcessesParams: {
                        type: 'device',
                        privileges: Ext.encode(me.getPrivileges())
                    },
                    startProcessParams: [
                        {
                            name: 'type',
                            value: 'device'
                        },
                        {
                            name: 'id',
                            value: 'deviceId'
                        }
                    ],
                    additionalReasons: [Uni.I18n.translate('startProcess.empty.list.item', 'MDC', 'No processes are available for the current device state.')],
                }

            },
            {
                xtype: 'add-to-zone-panel',
                itemId: 'device-zone-add-panel',
            },
            {
                xtype: 'issue-manually-creation-rules-item-add',
                itemId: 'issue-manually-creation-rules-item-add-bulk',
                title: '',
                bulkAction: true
            },
            {
                xtype: 'form',
                itemId: 'load-profile-panel',
                hidden: true,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'lp-selection',
                        name: 'lp-id',
                        fieldLabel: Uni.I18n.translate('searchItems.bulk.loadProfile', 'MDC', 'Load profile'),
                        emptyText: Uni.I18n.translate('searchItems.bulk.selectLoadProfile', 'MDC', 'Select a load profile...'),
                        required: true,
                        width: 200 + 256,
                        allowBlank: false,
                        queryMode: 'local',
                        store: 'Mdc.store.LoadProfilesOfDevice',
                        displayField: 'name',
                        valueField: 'id',
                        listConfig: {
                            loadMask: false
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'lp-next-start',
                        margin: '10 0 10 0',
                        fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'lp-date-picker',
                                name: 'lp-msec',
                                valueInMilliseconds: true,
                                layout: 'hbox',
                                labelAlign: 'left',
                                style: {
                                    border: 'none',
                                    padding: 0,
                                    marginBottom: '10px'
                                },
                                dateConfig: {
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault),
                                    width: 155
                                },
                                hoursConfig: {
                                    width: 60
                                },
                                minutesConfig: {
                                    width: 60
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'lp-no-lp-warning',
                        fieldLabel: Uni.I18n.translate('searchItems.bulk.loadProfile', 'MDC', 'Load profile'),
                        value: Uni.I18n.translate('searchItems.bulk.noLoadProfilesDefined', 'MDC', 'The device configuration has no load profiles defined.'),
                        fieldStyle: 'color: #eb5642',
                        required: true,
                        hidden: true
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'device-send-sap-notification-bulk',
                buttonAlign: 'left',
                hidden: true,
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                defaults: {
                    labelWidth: 150,
                    width: 800
                },
                items: [
                            {
                                xtype: 'displayfield',
                                required: true,
                                fieldLabel: Uni.I18n.translate('sap.webserviceendpoint', 'MDC', 'Web service endpoint'),
                                itemId: 'deviceSendSapNotificationEndpointNoItemBulk',
                                value: Uni.I18n.translate('sap.nowebserviceendpoint', 'MDC', 'No active web service endpoints available'),
                                fieldStyle: 'color: #ff0000;'
                            },
                            {
                                xtype: 'combobox',
                                editable: false,
                                required: true,
                                fieldLabel: Uni.I18n.translate('sap.webserviceendpoint', 'MDC', 'Web service endpoint'),
                                displayField: 'name',
                                valueField: 'id',
                                queryMode: 'local',
                                itemId: 'deviceSendSapNotificationEndpointComboBulk',
                                allowBlank: false,
                                emptyText: Uni.I18n.translate('sap.selectwebserviceendpoint', 'MDC', 'Select a web service endpoint'),
                                width: 500,
                                name: 'id'
                            },
                        ]
             },
             {
                 xtype: 'form',
                 itemId: 'device-set-push-events-to-sap-bulk',
                 buttonAlign: 'left',
                 hidden: true,
                 layout: {
                     type: 'vbox',
                     align: 'left'
                 },
                 defaults: {
                     labelWidth: 200,
                 },
                 items: [
                             {
                                 xtype: 'checkbox',
                                 name: 'pusheventstosap',
                                 itemId: 'pushEventsToSapCheckbox',
                                 fieldLabel: Uni.I18n.translate('sap.pusheventstosap', 'MDC', 'Push events to SAP')
                             },
                         ]
            }
        ];
        me.callParent(arguments);
    },
    getPrivileges: function () {
        var executionPrivileges = [];

        Dbp.privileges.DeviceProcesses.canExecuteLevel1() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel1.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel2() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel2.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel3() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel3.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel4() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel4.toString()});

        return executionPrivileges;
    }
});