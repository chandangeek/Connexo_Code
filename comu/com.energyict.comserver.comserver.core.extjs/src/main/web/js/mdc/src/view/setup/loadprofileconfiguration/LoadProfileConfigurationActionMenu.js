Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.load-profile-configuration-action-menu',
    itemId: 'load-profile-configuration-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editloadprofileconfigurationondeviceconfiguration',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteloadprofileconfigurationondeviceonfiguration',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});