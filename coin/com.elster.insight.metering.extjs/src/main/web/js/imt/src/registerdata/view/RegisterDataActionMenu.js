Ext.define('Imt.registerdata.view.RegisterDataActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.registerDataActionMenu',
    itemId: 'registerDataActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'confirm-value',
           // hidden: true,
            text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
            action: 'confirmValue'
        },
        {
            itemId: 'editData',
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
            action: 'editData'
        },
        {
            itemId: 'removeData',
            text: Uni.I18n.translate('general.remove', 'IMT', 'Remove'),
            action: 'removeData'
        }
    ]
});
