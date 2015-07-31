Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.change-device-life-cycle-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: false,
    items: [
        {
            itemId: 'select-device-life-cycle',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('deviceLifeCycle.select', 'MDC', 'Select device life cycle')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('validationResults.status', 'MDC', 'Status')
        }
    ]
});