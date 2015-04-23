Ext.define('Mdc.view.setup.deviceregisterconfiguration.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceRegisterConfigurationActionMenu',
    itemId: 'deviceRegisterConfigurationActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
            action: 'viewSuspects'
        },
        {
            itemId: 'validateNowRegister',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            privileges: Cfg.privileges.Validation.validateManual,
            action: 'validate'
        }
    ]
});
