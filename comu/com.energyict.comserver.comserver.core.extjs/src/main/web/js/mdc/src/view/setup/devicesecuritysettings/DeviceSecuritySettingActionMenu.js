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
            itemId: 'editDeviceSecuritySetting',
            action: 'editDeviceSecuritySetting'
        },
        {
            text: Uni.I18n.translate('general.showEncryptedValue', 'MDC', 'Show values'),
            itemId: 'showValueDeviceSecuritySetting',
            action: 'showValueDeviceSecuritySetting',
            hidden: true
        },
        {
            text: Uni.I18n.translate('general.hideEncryptedValue', 'MDC', 'Hide values'),
            itemId: 'hideValueDeviceSecuritySetting',
            action: 'hideValueDeviceSecuritySetting',
            hidden: true
        }
    ]
});

