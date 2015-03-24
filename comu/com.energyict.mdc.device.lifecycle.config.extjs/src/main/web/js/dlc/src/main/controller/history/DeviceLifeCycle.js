Ext.define('Dlc.main.controller.history.DeviceLifeCycle', {
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
                    controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                    action: 'showDeviceLifeCycles',
                    items: {
                        devicelifecycle: {
                            route: '{devicelifecycleId}',
                            controller: 'Dlc.controller.DeviceLifeCycles',
                            action: 'showDeviceLifeCycleDetails',
                            callback: function (route) {
                                this.getApplication().on('devicelifecycleload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                states: {
                                    title: Uni.I18n.translate('general.states', 'DLC', 'States'),
                                    route: 'states',
                                    controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                    action: 'showDeviceLifeCycleStates'
                                },
                                transitions: {
                                    title: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
                                    route: 'transitions',
                                    controller: 'Dlc.controller.DeviceLifeCycles',
                                    action: 'showDeviceLifeCycleTransitions'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
