Ext.define('Dbp.controller.History', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,
    requires: [],
    routeConfig: {
        'devices/device/processes': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processesrunning': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/running',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processeshistory': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/history',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            filter: 'Dbp.deviceprocesses.model.HistoryProcessesFilter',
            action: 'showDeviceProcesses'
        },
		administration: {
            title: Uni.I18n.translate('general.administration','DBP','Administration'),
            route: 'administration',
            disabled: true,
            items: {                
				managementprocesses:{
					title: Uni.I18n.translate('bpm.process.title', 'DBP', 'Processes'),
                    route: 'managementprocesses',
                    controller: 'Dbp.processes.controller.Processes',
                    action: 'showProcesses',
                    privileges: Dbp.privileges.DeviceProcesses.viewProcesses,
					items: {
                        edit: {
                            title: Uni.I18n.translate('bpm.editprocess.title', 'DBP', 'Edit process'),
                            route: '{processId}/edit',
                            controller: 'Dbp.processes.controller.Processes',
                            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                            action: 'editProcess',
                            params: {
                                activate: false
                            },
                            callback: function (route) {
                                this.getApplication().on('editProcesses', function (record) {
                                    route.setTitle(Uni.I18n.translate('editProcess.edit', 'DBP', "Edit '{0}'", record.get('name')));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items : {
                                deviceStates: {
                                    title: Uni.I18n.translate('general.addDeviceStates','DBP','Add device states'),
                                    route: 'deviceStates',
                                    controller: 'Dbp.processes.controller.Processes',
                                    privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                                    action: 'addDeviceStates',
                                    params: {
                                        activate: false
                                    }
                                },
                                privileges: {
                                    title: Uni.I18n.translate('general.addPrivileges','DBP','Add privileges'),
                                    route: 'privileges',
                                    controller: 'Dbp.processes.controller.Processes',
                                    privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                                    action: 'addPrivileges',
                                    params: {
                                        activate: false
                                    }

                                }
                            }
                        },
                        activate: {
                            title: Uni.I18n.translate('bpm.editprocess.title', 'DBP', 'Edit process'),
                            route: '{processId}/activate',
                            controller: 'Dbp.processes.controller.Processes',
                            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                            action: 'editProcess',
                            params: {
                                activate: true
                            },
                            callback: function (route) {
                                this.getApplication().on('activateProcesses', function (record) {
                                    route.setTitle(Uni.I18n.translate('editProcess.activate', 'DBP', "Activate '{0}'", record.get('name')));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items : {
                                deviceStates: {
                                    title: Uni.I18n.translate('general.addDeviceStates','DBP','Add device states'),
                                    route: 'deviceStates',
                                    controller: 'Dbp.processes.controller.Processes',
                                    privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                                    action: 'addDeviceStates',
                                    params: {
                                        activate: true
                                    }
                                },
                                privileges: {
                                    title: Uni.I18n.translate('general.addPrivileges','DBP','Add privileges'),
                                    route: 'privileges',
                                    controller: 'Dbp.processes.controller.Processes',
                                    privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                                    action: 'addPrivileges',
                                    params: {
                                        activate: true
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

