Ext.define('Mdc.view.setup.deviceregisterdata.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceregisterdataactionmenu',
    itemId: 'deviceregisterdataactionmenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
            action: 'confirmValue'
        },
        {
            itemId: 'editData',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editData'
        },
        {
            itemId: 'removeData',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'removeData'
        }
    ]
});
