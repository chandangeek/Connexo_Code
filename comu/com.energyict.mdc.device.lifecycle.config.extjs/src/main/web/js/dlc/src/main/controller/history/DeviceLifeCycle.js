Ext.define('Dlc.main.controller.history.DeviceLifeCycle', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'DLC', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                devicelifecycles: {
                    title: Uni.I18n.translate('general.deviceLifeCycles', 'DLC', 'Device life cycles'),
                    route: 'devicelifecycles',
                    controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                    privileges: Dlc.privileges.DeviceLifeCycle.view,
                    action: 'showDeviceLifeCycles',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                            route: 'add',
                            controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                            privileges: Dlc.privileges.DeviceLifeCycle.configure,
                            action: 'showAddDeviceLifeCycle'
                        },
                        clone: {
                            title: Uni.I18n.translate('general.cloneDeviceLifeCycle', 'DLC', 'Clone device life cycle'),
                            route: '{deviceLifeCycleId}/clone',
                            controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                            privileges: Dlc.privileges.DeviceLifeCycle.configure,
                            action: 'showCloneDeviceLifeCycle',
                            callback: function (route) {
                                this.getApplication().on('devicelifecyclecloneload', function (title) {
                                    route.setTitle(Ext.String.htmlEncode(title));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        devicelifecycle: {
                            route: '{deviceLifeCycleId}',
                            controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                            privileges: Dlc.privileges.DeviceLifeCycle.view,
                            action: 'showDeviceLifeCycleOverview',
                            callback: function (route) {
                                this.getApplication().on('devicelifecycleload', function (record) {
                                    route.setTitle(Ext.String.htmlEncode(record.get('name')));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                                    route: 'edit',
                                    controller: 'Dlc.devicelifecycles.controller.DeviceLifeCycles',
                                    privileges: Dlc.privileges.DeviceLifeCycle.configure,
                                    action: 'showEditDeviceLifeCycle',
                                    callback: function (route) {
                                        this.getApplication().on('deviceLifeCycleEdit', function (record) {
                                            route.setTitle(Uni.I18n.translate('deviceLifeCycles.edit.title', 'DLC', "Edit '{0}'", [record.get('name')]));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
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
                                            action: 'showDeviceLifeCycleStateEdit',
                                            items: {
                                                addEntryProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                                    route: 'entryprocesses',
                                                    controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                                    action: 'showAvailableEntryTransitionProcesses'
                                                },
                                                addExitProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                                    route: 'exitprocesses',
                                                    controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                                    action: 'showAvailableExitTransitionProcesses'
                                                }
                                            }
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('deviceLifeCycleStates.edit', 'DLC', 'Edit state'),
                                            route: '{id}/edit',
                                            controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                            action: 'showDeviceLifeCycleStateEdit',
                                            callback: function (route) {
                                                this.getApplication().on('loadlifecyclestate', function (record) {
                                                    route.setTitle(Uni.I18n.translate('deviceLifeCycles.edit.title', 'DLC', "Edit '{0}'", [record.get('name')]));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                addEntryProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                                    route: 'entryprocesses',
                                                    controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                                    action: 'showAvailableEntryTransitionProcesses'
                                                },
                                                addExitProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                                    route: 'exitprocesses',
                                                    controller: 'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
                                                    action: 'showAvailableExitTransitionProcesses'
                                                }
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
                                            title: Uni.I18n.translate('general.addTransition', 'DLC', 'Add transition'),
                                            route: 'add',
                                            controller: 'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition'
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                                            route: '{transitionId}/edit',
                                            controller: 'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition',
                                            callback: function (route) {
                                                this.getApplication().on('deviceLifeCycleTransitionEdit', function (record) {
                                                    route.setTitle(Uni.I18n.translate('deviceLifeCycles.edit.title', 'DLC', "Edit '{0}'", [record.get('name')]));
                                                    return true;
                                                }, {single: true});
                                                return this;
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
    }
});
