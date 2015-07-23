Ext.define('Mdc.view.setup.devicechannels.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfileChannelsActionMenu',
    itemId: 'deviceLoadProfileChannelsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
            action: 'viewSuspects'
        },
        {
            itemId: 'validateNowChannel',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            privileges:Cfg.privileges.Validation.validateManual,
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions,
            action: 'validateNow'
        }
    ]
});
