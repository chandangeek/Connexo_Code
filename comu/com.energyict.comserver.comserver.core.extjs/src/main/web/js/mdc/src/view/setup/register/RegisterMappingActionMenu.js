Ext.define('Mdc.view.setup.register.RegisterMappingActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-mapping-action-menu',
    itemId: 'register-mapping-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'edit-register-mapping-btn-id',
                action: 'editTheRegisterMapping',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'removeRegisterMapping',
                action: 'removeTheRegisterMapping',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
