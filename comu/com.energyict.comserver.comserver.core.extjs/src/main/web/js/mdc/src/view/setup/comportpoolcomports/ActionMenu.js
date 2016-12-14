Ext.define('Mdc.view.setup.comportpoolcomports.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comPortPoolComPortsActionMenu',
    initComponent: function () {
        this.items = [
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
