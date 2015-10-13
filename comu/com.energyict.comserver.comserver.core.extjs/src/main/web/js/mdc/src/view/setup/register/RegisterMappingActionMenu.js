Ext.define('Mdc.view.setup.register.RegisterMappingActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.register-mapping-action-menu',
    plain: true,
    border: false,
    itemId: 'register-mapping-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'edit-register-mapping-btn-id',
            action: 'editTheRegisterMapping'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'removeRegisterMapping',
            action: 'removeTheRegisterMapping'

        }
    ]
});
