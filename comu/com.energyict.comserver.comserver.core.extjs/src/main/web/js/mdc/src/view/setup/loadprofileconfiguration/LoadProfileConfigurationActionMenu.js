Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.load-profile-configuration-action-menu',
    plain: true,
    border: false,
    itemId: 'load-profile-configuration-action-menu',
    shadow: false,
    items: [

        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editloadprofileconfigurationondeviceconfiguration'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteloadprofileconfigurationondeviceonfiguration'
        }

    ]
});