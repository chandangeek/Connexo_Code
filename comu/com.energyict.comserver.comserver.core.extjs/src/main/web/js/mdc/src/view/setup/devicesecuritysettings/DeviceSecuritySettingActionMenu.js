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
            hidden: !Uni.Auth.hasAnyPrivilege(['edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level3','edit.device.security.properties.level4']),
            itemId: 'editDeviceSecuritySetting',
            action: 'editDeviceSecuritySetting'
        },
        {
            text: Uni.I18n.translate('general.showEncryptedValue', 'MDC', 'Show values'),
            hidden: !Uni.Auth.hasAnyPrivilege(['view.device.security.properties.level1','view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4']),
            itemId: 'showValueDeviceSecuritySetting',
            action: 'showValueDeviceSecuritySetting',
            hidden: true
        },
        {
            text: Uni.I18n.translate('general.hideEncryptedValue', 'MDC', 'Hide values'),
            hidden: !Uni.Auth.hasAnyPrivilege(['view.device.security.properties.level1','view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4']),
            itemId: 'hideValueDeviceSecuritySetting',
            action: 'hideValueDeviceSecuritySetting',
            hidden: true
        }
    ]
});

