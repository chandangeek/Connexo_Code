Ext.define('Mdc.view.setup.deviceloadprofilechannels.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfileChannelsActionMenu',
    itemId: 'deviceLoadProfileChannelsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewData',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewData', 'MDC', 'View data'),
            action: 'viewData'
        },
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
            action: 'viewSuspects'
        },
        {
            itemId: 'viewDetails',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewDetails', 'MDC', 'View details'),
            action: 'viewDetails'
        },
        {
            itemId: 'validateNowChannel',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.view.validateManual'),
            action: 'validateNow'
        }
    ]
});
