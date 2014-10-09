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
            itemId: 'viewChannels',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewChannels', 'MDC', 'View channels'),
            action: 'viewChannels'
        },
        {
            itemId: 'viewData',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewData', 'MDC', 'View data'),
            action: 'viewData'
        },
        {
            itemId: 'viewDetails',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewDetails', 'MDC', 'View details'),
            action: 'viewDetails'
        },
        {
            itemId: 'validateNowLoadProfile',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
            action: 'validateNow'
        },
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
            action: 'viewSuspects'
        }
    ]
});
