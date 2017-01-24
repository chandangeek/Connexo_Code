Ext.define('Mdc.view.setup.comportpool.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comportpool-actionmenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'activate',
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate',
                text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                action: 'deactivate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
