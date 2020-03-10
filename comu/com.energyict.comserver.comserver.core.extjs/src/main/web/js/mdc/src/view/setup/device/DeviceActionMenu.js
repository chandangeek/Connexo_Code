/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-action-menu',
    items: [],
    disableChangeConfigSinceSlave: false,
    actionsStore: undefined,
    deviceName: undefined,
    router: undefined,

    initComponent: function() {
        var me = this,
            changeConfigItem = {
                itemId: 'change-device-configuration-action-item',
                privileges: Mdc.privileges.Device.changeDeviceConfiguration,
                dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.changeDeviceConfiguration,
                text: Uni.I18n.translate('deviceconfiguration.changeDeviceConfiguration', 'MDC', 'Change device configuration'),
                handler: function () {
                    me.router.getRoute('devices/device/changedeviceconfiguration').forward();
                },
                section: me.SECTION_EDIT
            };
        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                privileges: Mdc.privileges.Device.editDeviceAttributes,
                handler: function() {
                    me.router.getRoute('devices/device/attributes/edit').forward();
                },
                text: Uni.I18n.translate('deviceconfiguration.deviceAttributes.editAttributes', 'MDC', 'Edit attributes'),
                section: me.SECTION_EDIT
            },
            changeConfigItem,
            {
                itemId: 'mdc-show-device-network',
                // privileges: Mdc.privileges.Device.editDeviceAttributes,
                handler: function() {
                    me.router.getRoute('devices/device/network').forward();
                },
                text: Uni.I18n.translate('general.showNetwork', 'MDC', 'Show network'),
                section: me.SECTION_VIEW
            },
            {
                itemId: 'mdc-send-registered-sap-notification',
                // privileges: Mdc.privileges.Device.editDeviceAttributes,
                handler: function() {
                    me.router.getRoute('devices/device/sendregisteredsapnotification').forward();
                },
                text: Uni.I18n.translate('general.sendRegisteredSAPNotification', 'MDC', 'Send registered notification to SAP'),
                section: me.SECTION_VIEW
            }
        ];

        me.actionsStore.each(function (item) {
            me.items.push({
                itemId: 'action-menu-item' + item.get('id'),
                text: item.get('name'),
                section: me.SECTION_ACTION,
                handler: function () {
                    me.router.getRoute('devices/device/transitions').forward({transitionId: item.get('id')});
                }
            });
        });

        if (Mdc.privileges.Device.canViewProcessMenu()) {
            me.items.push({
                itemId: 'action-menu-item-start-proc',
                privileges: Mdc.privileges.Device.deviceProcesses && Mdc.privileges.Device.deviceExecuteProcesses,
                text: Uni.I18n.translate('deviceconfiguration.process.startProcess', 'MDC', 'Start process'),
                href: '#/devices/' + encodeURIComponent(me.deviceName) + '/processes/start',
                section: me.SECTION_ACTION
            });
        }

        if (Isu.privileges.Issue.canCreateManualIssue()) {
            me.items.push({
                itemId: 'device-issue-link',
                privileges: Isu.privileges.Issue.createManualIssue,
                text: Uni.I18n.translate('searchItems.bulk.newManuallyIssue', 'MDC', 'Create issue'),
                href: '#/devices/' + encodeURIComponent(me.deviceName) + '/newissuemanually',
                section: me.SECTION_ACTION
            });
        }

        me.callParent(arguments);
    }
});


