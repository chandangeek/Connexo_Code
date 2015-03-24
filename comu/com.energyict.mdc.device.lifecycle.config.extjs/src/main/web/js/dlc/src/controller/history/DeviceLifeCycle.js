Ext.define('Dlc.controller.history.DeviceLifeCycle', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                devicelifecycles: {
                    title: Uni.I18n.translate('general.deviceLifeCycles', 'DLC', 'Device life cycles'),
                    route: 'devicelifecycles',
                    controller: 'Dlc.controller.DeviceLifeCycles',
                    action: 'showDeviceLifeCycles'
                }
            }
        }
    }
});
