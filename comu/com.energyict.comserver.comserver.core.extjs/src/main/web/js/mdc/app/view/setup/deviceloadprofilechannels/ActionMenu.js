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
            itemId: 'viewDetails',
            text: Uni.I18n.translate('deviceloadprofiles.actionmenu.viewDetails', 'MDC', 'View details'),
            action: 'viewDetails'
        }
    ]
});
