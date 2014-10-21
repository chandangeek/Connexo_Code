Ext.define('Mdc.view.setup.devicegroup.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.devicegroup-add-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: true,
    items: [
        {
            itemId: 'General',
            text: Uni.I18n.translate('devicegroup.add.general', 'MDC', 'General')
        },
        {
            itemId: 'DeviceGroup',
            text: Uni.I18n.translate('devicegroup.add.devicegroup', 'MDC', 'Device group')
        }
    ]

});
