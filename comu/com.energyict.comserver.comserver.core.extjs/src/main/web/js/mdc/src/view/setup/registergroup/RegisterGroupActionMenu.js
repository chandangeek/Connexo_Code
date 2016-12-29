Ext.define('Mdc.view.setup.registergroup.RegisterGroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.register-group-action-menu',
    itemId: 'register-group-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editRegisterGroup',
                action: 'editRegisterGroup',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'removeRegisterGroup',
                action: 'removeRegisterGroup',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
