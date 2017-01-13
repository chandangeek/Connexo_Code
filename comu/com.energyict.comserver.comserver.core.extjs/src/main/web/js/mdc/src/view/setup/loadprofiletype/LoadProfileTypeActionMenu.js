Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.load-profile-type-action-menu',
    itemId: 'load-profile-type-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editloadprofiletype',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteloadprofiletype',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
