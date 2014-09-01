Ext.define('Mdc.view.setup.devicelogbooks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLogbooksActionMenu',
    itemId: 'deviceLogbooksActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewData',
            text: Uni.I18n.translate('devicelogbooks.actionmenu.viewData', 'MDC', 'View data'),
            action: 'viewData'
        },
        {
            itemId: 'viewDetails',
            text: Uni.I18n.translate('devicelogbooks.actionmenu.viewDetails', 'MDC', 'View details'),
            action: 'viewDetails'
        }
    ]
});
