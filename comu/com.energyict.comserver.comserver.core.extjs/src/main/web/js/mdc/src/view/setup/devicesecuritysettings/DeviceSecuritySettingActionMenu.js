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
        }
    ]
});

