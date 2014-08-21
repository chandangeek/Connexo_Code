Ext.define('Mdc.view.setup.deviceregisterconfiguration.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceRegisterConfigurationActionMenu',
    itemId: 'deviceRegisterConfigurationActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewdata', 'MDC', 'View data'),
            action: 'viewdata'
        },
        {
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            action: 'validate'
        },
        {
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
            action: 'viewsuspects'
        }
    ]
});
