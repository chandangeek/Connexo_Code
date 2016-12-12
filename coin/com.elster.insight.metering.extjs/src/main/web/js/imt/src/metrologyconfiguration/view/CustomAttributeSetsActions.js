Ext.define('Imt.metrologyconfiguration.view.CustomAttributeSetsActions', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.custom-attribute-sets-actions',
    initComponent: function () {
        this.items = [
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                action: 'removeCustomAttributeSet',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
