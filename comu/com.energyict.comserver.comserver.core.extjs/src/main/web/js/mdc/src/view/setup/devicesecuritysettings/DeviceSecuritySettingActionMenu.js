/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-security-setting-action-menu',
    itemId: 'device-security-setting-action-menu',

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.DeviceSecurity.editLevels,
                itemId: 'editDeviceSecuritySetting',
                action: 'editDeviceSecuritySetting',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.securitySettingsActions,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.showEncryptedValue', 'MDC', 'Show values'),
                privileges: Mdc.privileges.DeviceSecurity.viewLevels,
                itemId: 'showValueDeviceSecuritySetting',
                action: 'showValueDeviceSecuritySetting',
                hidden: true,
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.hideEncryptedValue', 'MDC', 'Hide values'),
                privileges: Mdc.privileges.DeviceSecurity.viewLevels,
                itemId: 'hideValueDeviceSecuritySetting',
                action: 'hideValueDeviceSecuritySetting',
                hidden: true,
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});

