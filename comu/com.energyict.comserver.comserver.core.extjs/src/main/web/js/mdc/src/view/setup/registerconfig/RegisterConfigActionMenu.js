Ext.define('Mdc.view.setup.registerconfig.RegisterConfigActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-config-action-menu',
    itemId: 'register-config-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editRegisterConfig',
                action: 'editRegisterConfig',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'deleteRegisterConfig',
                action: 'deleteRegisterConfig',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});