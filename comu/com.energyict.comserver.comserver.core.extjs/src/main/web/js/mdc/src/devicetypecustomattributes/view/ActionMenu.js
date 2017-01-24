Ext.define('Mdc.devicetypecustomattributes.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-type-custom-attribute-sets-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'device-type-custom-attribute-sets-remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});