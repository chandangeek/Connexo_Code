Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-protocol-dialect-action-menu',
    itemId: 'device-protocol-dialect-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editDeviceProtocolDialect',
                action: 'editDeviceProtocolDialect',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
