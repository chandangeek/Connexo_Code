Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.protocol-dialect-action-menu',
    plain: true,
    border: false,
    itemId: 'protocol-dialect-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editProtocolDialect',
            action: 'editProtocolDialect'

        }
    ]
});
