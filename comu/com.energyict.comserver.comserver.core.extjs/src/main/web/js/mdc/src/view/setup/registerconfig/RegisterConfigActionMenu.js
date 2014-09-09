Ext.define('Mdc.view.setup.registerconfig.RegisterConfigActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.register-config-action-menu',
    plain: true,
    border: false,
    itemId: 'register-config-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editRegisterConfig',
            action: 'editRegisterConfig'

        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteRegisterConfig',
            action: 'deleteRegisterConfig'

        }
    ]
});