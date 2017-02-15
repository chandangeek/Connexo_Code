/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanningGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.DeviceCommunicationPlanningGrid',
    ui: 'large',
    cls: 'no-side-padding',
    title: Uni.I18n.translate('deviceCommunicationPlanning.title', 'MDC', 'Communication planning'),

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action'
    ],

    scheduleStore: undefined,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                itemId: 'mdc-device-communication-planning-grid-msg',
                hidden: true,
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('deviceCommunicationPlanning.noCommunicationTasks', 'MDC', 'This device has no communication tasks.')
                    }
                ]
            },
            {
                xtype: 'grid',
                store: me.scheduleStore,
                columns: [
                    {
                        header: Uni.I18n.translate('deviceCommunicationSchedules.communicationTask', 'MDC', 'Commmunication task'),
                        flex: 2,
                        dataIndex: 'comTask',
                        renderer: function (value) {
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }
                            return Ext.String.htmlEncode(value.name);
                        }
                    },
                    {
                        header: Uni.I18n.translate('general.schedule', 'MDC', 'Schedule'),
                        flex: 2,
                        dataIndex: 'schedule',
                        renderer: function (value, meta, record) {
                            if (Ext.isEmpty(value) || record.get('type') === 'ONREQUEST') {
                                return '-';
                            }
                            return Mdc.util.ScheduleToStringConverter.convert(value, true);
                        }
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationPlanning.sharedCommunicationSchedule', 'MDC', 'Shared commmunication schedule'),
                        dataIndex: 'name',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationPlanning.plannedDate', 'MDC', 'Planned date'),
                        dataIndex: 'plannedDate',
                        flex: 1,
                        renderer: function (value, meta, record) {
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }

                            var plannedDate = moment(value).valueOf(),
                                nextCommunication = Ext.isEmpty(record.get('nextCommunication')) ? undefined : moment(record.get('nextCommunication')).valueOf();
                            if (plannedDate !== nextCommunication) {
                                var toolTip = '';
                                if (!record.get('active')) {
                                    toolTip = Uni.I18n.translate('deviceCommunicationPlanning.tooltip.inactiveCommunicationTask',
                                        'MDC', 'This communication task is inactive on this device.');
                                } else if (record.get('hasConnectionWindow')) {
                                    toolTip = Uni.I18n.translate("deviceCommunicationPlanning.tooltip.connectionWindow",
                                            "MDC", "Connection method has a connection window.")
                                        + '&lt;br/&gt;'
                                        + Uni.I18n.translate('deviceCommunicationPlanning.tooltip.nextCommunication',
                                            'MDC', 'The next communication is on {0}.', Uni.DateTime.formatDateTimeShort(new Date(nextCommunication)));
                                } else {
                                    toolTip = Uni.I18n.translate("deviceCommunicationPlanning.tooltip.minimiseConnections",
                                            "MDC", "Connection method has the strategy 'Minimise connections'.")
                                        + '&lt;br/&gt;'
                                        + Uni.I18n.translate('deviceCommunicationPlanning.tooltip.nextCommunication',
                                            'MDC', 'The next communication is on {0}.', Uni.DateTime.formatDateTimeShort(new Date(nextCommunication)));
                                }

                                return '<span style="display:inline-block; float:left; margin-right:7px;" >' + Uni.DateTime.formatDateTimeShort(new Date(value)) + '</span>' +
                                    '<span class="icon-warning" style="display:inline-block; color:#EB5642; font-size:16px;" data-qtip="' + toolTip + '"></span>';
                            }
                            return Uni.DateTime.formatDateTimeShort(new Date(value));
                        }
                    },
                    {
                        xtype: 'uni-actioncolumn',
                        menu: {
                            plain: true,
                            border: false,
                            shadow: false,
                            items: [
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.runComTask', 'MDC', 'Run'),
                                    privileges: Mdc.privileges.Device.operateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-runDeviceComTask',
                                    action: 'runDeviceComTask',
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.runComTaskNow', 'MDC', 'Run now'),
                                    privileges: Mdc.privileges.Device.operateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-runDeviceComTaskNow',
                                    action: 'runDeviceComTaskNow',
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.activate', 'MDC', 'Activate'),
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-activate-task',
                                    action: 'activateComTask'
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.deactivate', 'MDC', 'Deactivate'),
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-deactivate-task',
                                    action: 'deactivateComTask'
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.addSchedule', 'MDC', 'Add schedule'),
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-add-schedule',
                                    action: 'addSchedule'
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.changeSchedule', 'MDC', 'Change schedule'),
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-change-schedule',
                                    action: 'changeSchedule'
                                },
                                {
                                    text: Uni.I18n.translate('deviceCommunicationPlanning.removeSchedule', 'MDC', 'Remove schedule'),
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    itemId: 'mdc-device-communication-planning-remove-schedule',
                                    action: 'removeSchedule'
                                }
                            ],
                            listeners: {
                                beforeshow: function () {
                                    var me = this,
                                        activateMenuItem = me.down('#mdc-device-communication-planning-activate-task'),
                                        deactivateMenuItem = me.down('#mdc-device-communication-planning-deactivate-task'),
                                        runMenuItem = me.down('#mdc-device-communication-planning-runDeviceComTask'),
                                        runNowMenuItem = me.down('#mdc-device-communication-planning-runDeviceComTaskNow'),
                                        addScheduleMenuItem = me.down('#mdc-device-communication-planning-add-schedule'),
                                        changeScheduleMenuItem = me.down('#mdc-device-communication-planning-change-schedule'),
                                        removeScheduleMenuItem = me.down('#mdc-device-communication-planning-remove-schedule'),
                                        taskType = me.record.get('type'),
                                        addScheduleVisible = taskType === 'ONREQUEST' || taskType === 'ADHOC',
                                        changeAndRemoveSchedulePossible = taskType === 'INDIVIDUAL',
                                        isActive = me.record.get('active'),
                                        connectionDefinedOnDevice = me.record.get('connectionDefinedOnDevice'),
                                        isMinimize = !connectionDefinedOnDevice ? false : me.record.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS',

                                        runNowEnabled = isActive && connectionDefinedOnDevice,
                                        runEnabled = runNowEnabled && isMinimize,
                                        activateEnabled = !Ext.isEmpty(activateMenuItem) && !isActive,
                                        deactivateEnabled = !Ext.isEmpty(deactivateMenuItem) && isActive,
                                        addScheduleEnabled = !Ext.isEmpty(addScheduleMenuItem) && addScheduleVisible,
                                        changeScheduleEnabled = !Ext.isEmpty(changeScheduleMenuItem) && changeAndRemoveSchedulePossible,
                                        removeScheduleEnabled = !Ext.isEmpty(removeScheduleMenuItem) && changeAndRemoveSchedulePossible;

                                    if (runEnabled) {
                                        runMenuItem.show();
                                    } else {
                                        runMenuItem.hide();
                                    }
                                    if (runNowEnabled) {
                                        runNowMenuItem.show();
                                    } else {
                                        runNowMenuItem.hide();
                                    }
                                    if (addScheduleEnabled) {
                                        addScheduleMenuItem.show();
                                    } else {
                                        addScheduleMenuItem.hide();
                                    }
                                    if (changeScheduleEnabled) {
                                        changeScheduleMenuItem.show();
                                    } else {
                                        changeScheduleMenuItem.hide();
                                    }
                                    if (removeScheduleEnabled) {
                                        removeScheduleMenuItem.show();
                                    } else {
                                        removeScheduleMenuItem.hide();
                                    }
                                    if (activateEnabled) {
                                        activateMenuItem.show();
                                    } else {
                                        activateMenuItem.hide();
                                    }
                                    if (deactivateEnabled) {
                                        deactivateMenuItem.show();
                                    } else {
                                        deactivateMenuItem.hide();
                                    }
                                }
                            }
                        },
                        isDisabled: function (view, rowIndex, callIndex, item, record) {
                            var me = this,
                                activateMenuItem = me.menu.down('#mdc-device-communication-planning-activate-task'),
                                deactivateMenuItem = me.menu.down('#mdc-device-communication-planning-deactivate-task'),
                                addScheduleMenuItem = me.menu.down('#mdc-device-communication-planning-add-schedule'),
                                changeScheduleMenuItem = me.menu.down('#mdc-device-communication-planning-change-schedule'),
                                removeScheduleMenuItem = me.menu.down('#mdc-device-communication-planning-remove-schedule'),
                                isActive = record.get('active'),
                                connectionDefinedOnDevice = record.get('connectionDefinedOnDevice'),
                                isMinimize = !connectionDefinedOnDevice ? false : record.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS',
                                taskType = record.get('type'),
                                addScheduleVisible = taskType === 'ONREQUEST' || taskType === 'ADHOC',
                                changeAndRemoveScheduleVisible = taskType === 'INDIVIDUAL',

                                runNowEnabled = isActive && connectionDefinedOnDevice,
                                runEnabled = runNowEnabled && isMinimize,
                                activateEnabled = !Ext.isEmpty(activateMenuItem) && !isActive,
                                deactivateEnabled = !Ext.isEmpty(deactivateMenuItem) && isActive,
                                addScheduleEnabled = !Ext.isEmpty(addScheduleMenuItem) && addScheduleVisible,
                                changeScheduleEnabled = !Ext.isEmpty(changeScheduleMenuItem) && changeAndRemoveScheduleVisible,
                                removeScheduleEnabled = !Ext.isEmpty(removeScheduleMenuItem) && changeAndRemoveScheduleVisible;

                            return !(runNowEnabled || runEnabled || activateEnabled || deactivateEnabled ||
                            addScheduleEnabled || changeScheduleEnabled || removeScheduleEnabled);
                        }
                    }
                ],

                dockedItems: [
                    {
                        xtype: 'pagingtoolbartop',
                        noBottomPaging: true,
                        exportButton: false,
                        store: me.scheduleStore,
                        dock: 'top',
                        displayMsg: Uni.I18n.translate('deviceCommunicationPlanning.displayMsg', 'MDC', '{0} - {1} of {2} communication tasks'),
                        displayMoreMsg: '',
                        emptyMsg: '',
                        items: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('deviceCommunicationPlanning.removeSharedSchedule', 'MDC', 'Remove shared communication schedule'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                action: 'removeSharedCommunicationSchedule',
                                itemId: 'mdc-device-communication-planning-removeSharedCommunicationScheduleButton'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('deviceCommunicationPlanning.addSharedSchedule', 'MDC', 'Add shared communication schedule'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                action: 'addSharedCommunicationSchedule',
                                itemId: 'mdc-device-communication-planning-addSharedCommunicationScheduleButton'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }

});
