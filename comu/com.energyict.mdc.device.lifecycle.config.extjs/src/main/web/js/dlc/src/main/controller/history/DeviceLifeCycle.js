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
                        add: {
                            title: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                            route: 'add',
                            controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                            action: 'showAddDeviceLifeCycle'
                        },
                        clone: {
                            title: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Clone device life cycle'),
                            route: '{deviceLifeCycleId}/clone',
                            controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                            action: 'showCloneDeviceLifeCycle',
                            callback: function (route) {
                                this.getApplication().on('devicelifecyclecloneload', function (title) {
                                    route.setTitle(title);
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        devicelifecycle: {
                            route: '{deviceLifeCycleId}',
                            redirect: 'administration/devicelifecycles/devicelifecycle/states',
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
                                    action: 'showDeviceLifeCycleStates',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                                            route: 'add',
                                            controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                            action: 'showDeviceLifeCycleStateEdit'
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('deviceLifeCycleStates.edit', 'DLC', 'Edit state'),
                                            route: '{id}/edit',
                                            controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                            action: 'showDeviceLifeCycleStateEdit',
                                            callback: function (route) {
                                                this.getApplication().on('loadlifecyclestate', function (title) {
                                                    route.setTitle(title);
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            }
                                        }
                                    }
                                },
                                transitions: {
                                    title: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
                                    route: 'transitions',
                                    controller: 'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                    action: 'showDeviceLifeCycleTransitions',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                                            route: 'add',
                                            controller: 'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition'
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                                            route: '{transitionId}/edit',
                                            controller: 'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
