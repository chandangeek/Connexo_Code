Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-security-setting-action-menu',
    plain: true,
    border: false,
    itemId: 'device-security-setting-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            privileges: Mdc.privileges.DeviceSecurity.editLevels,
            itemId: 'editDeviceSecuritySetting',
            action: 'editDeviceSecuritySetting',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.securitySettingsActions
        },
        {
            text: Uni.I18n.translate('general.showEncryptedValue', 'MDC', 'Show values'),
            privileges:Mdc.privileges.DeviceSecurity.viewLevels,
            itemId: 'showValueDeviceSecuritySetting',
            action: 'showValueDeviceSecuritySetting',
            hidden: true
        },
        {
            text: Uni.I18n.translate('general.hideEncryptedValue', 'MDC', 'Hide values'),
            privileges:Mdc.privileges.DeviceSecurity.viewLevels,
            itemId: 'hideValueDeviceSecuritySetting',
            action: 'hideValueDeviceSecuritySetting',
            hidden: true
        }
    ]
});

