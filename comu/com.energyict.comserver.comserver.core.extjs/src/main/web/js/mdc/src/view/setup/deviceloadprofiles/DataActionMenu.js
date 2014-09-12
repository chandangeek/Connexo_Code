Ext.define('Mdc.view.setup.deviceloadprofiles.DataActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfilesDataActionMenu',
    itemId: 'deviceLoadProfilesDataActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewHistory',
            text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'MDC', 'View history'),
            action: 'viewHistory'
        }
    ]
});
