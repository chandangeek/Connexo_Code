Ext.define('Mdc.view.setup.devicegroup.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.devicegroup-add-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    margin: '0 0 0 0',

    title: Uni.I18n.translate('devicegroup.wizardMenu', 'MDC', 'Add device group'),

    items: [
        {
            itemId: 'General',
            text: Uni.I18n.translate('devicegroup.add.general', 'MDC', 'General')
        },
        {
            itemId: 'DeviceGroup',
            text: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group')
        }
    ]
});
