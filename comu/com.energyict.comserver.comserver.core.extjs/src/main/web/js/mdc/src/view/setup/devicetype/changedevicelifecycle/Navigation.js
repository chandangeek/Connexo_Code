Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.change-device-life-cycle-navigation',
    jumpForward: false,
    jumpBack: true,
    items: [
        {
            itemId: 'select-device-life-cycle',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('deviceLifeCycle.select', 'MDC', 'Select device life cycle')
        },
        {
            itemId: 'map-states',
            action: 'mapStates',
            text: Uni.I18n.translate('deviceLifeCycle.mapStates', 'MDC', 'Map states')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('validationResults.status', 'MDC', 'Status')
        }
    ]
});