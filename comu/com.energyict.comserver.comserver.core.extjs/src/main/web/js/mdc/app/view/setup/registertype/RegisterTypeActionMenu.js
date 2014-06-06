Ext.define('Mdc.view.setup.registertype.RegisterTypeActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.register-type-action-menu',
    plain: true,
    border: false,
    itemId: 'register-type-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editRegisterType',
            action: 'editRegisterType'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteRegisterType',
            action: 'deleteRegisterType'

        }
    ]
});
