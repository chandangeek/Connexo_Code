Ext.define('Mdc.view.setup.deviceloadprofiles.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfilesActionMenu',
    itemId: 'deviceLoadProfilesActionMenu',
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
            itemId: 'validateNowLoadProfile',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            privileges: Cfg.privileges.Validation.validateManual,
            action: 'validateNow'
        }
    ]
});
