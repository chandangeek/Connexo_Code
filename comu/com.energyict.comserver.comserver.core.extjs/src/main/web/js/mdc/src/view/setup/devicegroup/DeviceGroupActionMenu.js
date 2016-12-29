Ext.define('Mdc.view.setup.devicegroup.DeviceGroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-group-action-menu',
    itemId: 'device-group-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editDeviceGroup',
                itemId: 'edit-device-group',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'remove-device-group',
                action: 'deleteDeviceGroup',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
