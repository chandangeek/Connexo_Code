Ext.define('Dbp.controller.History', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,
    requires: [],
    routeConfig: {
        'usagepoints/usagepoint/processes': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'usagepoints/{usagePointId}/processes',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showUsagePointProcesses'
        },
        'usagepoints/usagepoint/processesrunning': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'usagepoints/{usagePointId}/processes/running',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showUsagePointProcesses'
        },
        'usagepoints/usagepoint/processeshistory': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'usagepoints/{usagePointId}/processes/history',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            filter: 'Bpm.monitorprocesses.model.HistoryProcessesFilter',
            action: 'showUsagePointProcesses'
        },
        'devices/device/processes': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processesrunning': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/running',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processeshistory': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/history',
            controller: 'Dbp.monitorprocesses.controller.MonitorProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            filter: 'Bpm.monitorprocesses.model.HistoryProcessesFilter',
            action: 'showDeviceProcesses'
        },
        'devices/device/processstart': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/start',
            controller: 'Dbp.startprocess.controller.StartProcess',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showStartProcess'
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
                            route: '{name}/{version}/edit',
                            controller: 'Dbp.processes.controller.Processes',
                            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                            action: 'editProcess',
                            params: {
                                activate: false
                            },
                            callback: function (route) {
                                this.getApplication().on('editProcesses', function (name) {
                                    route.setTitle(Uni.I18n.translate('editProcess.edit', 'DBP', "Edit '{0}'", name));
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
                            route: '{name}/{version}/activate',
                            controller: 'Dbp.processes.controller.Processes',
                            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                            action: 'editProcess',
                            params: {
                                activate: true
                            },
                            callback: function (route) {
                                this.getApplication().on('activateProcesses', function (name) {
                                    route.setTitle(Uni.I18n.translate('editProcess.activate', 'DBP', "Activate '{0}'", name));
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

