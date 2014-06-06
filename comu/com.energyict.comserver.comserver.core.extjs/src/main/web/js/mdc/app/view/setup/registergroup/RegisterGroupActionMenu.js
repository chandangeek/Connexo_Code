Ext.define('Mdc.view.setup.registergroup.RegisterGroupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.register-group-action-menu',
    plain: true,
    border: false,
    itemId: 'register-group-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editRegisterGroup'
        }
    ]
});
