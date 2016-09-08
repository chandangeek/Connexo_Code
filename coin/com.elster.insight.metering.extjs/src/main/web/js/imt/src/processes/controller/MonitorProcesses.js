Ext.define('Imt.processes.controller.MonitorProcesses', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses',
        'Uni.property.controller.Registry'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    stores: [
    ],
    views: [
        'Imt.processes.view.UsagePointProcessesMainView',
        'Imt.processes.view.UsagePointStartProcess',
        'Imt.processes.view.MetrologyConfigurationOutputs'
    ],

    showUsagePointProcesses: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        viewport.setLoading();

        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagePoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-main-view', {
                    router: router,
                    usagePoint: usagePoint,
                    properties: {
                        variableId: 'usagePointId',
                        name: 'usagePoint',
                        value:  usagePoint.get('id'),
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute() ? 'workspace/tasks/performTask' : null
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointStartProcess: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        viewport.setLoading();

        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
                var widget;

                Uni.property.controller.Registry.addProperty('METROLOGYCONFIGOUTPUT', 'Imt.processes.view.MetrologyConfigurationOutputs');
                me.getApplication().fireEvent('usagePointLoaded', usagePoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-start', {
                    usagePoint: usagePoint,
                    router: router,
                    properties: {
                        activeProcessesParams: {
                            type: 'usagePoint',
                            metrologyConfigurations: usagePoint.raw.metrologyConfiguration ? usagePoint.raw.metrologyConfiguration.id : null,
                            privileges: Ext.encode(me.getPrivileges()),
                            connectionStates: usagePoint.get('connectionState').id
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'usagePoint'
                            },
                            {
                                name: 'id',
                                value: 'usagePointId'
                            },
                            {
                                name: 'value',
                                value: usagePoint.get('id')
                            }
                        ],
                        successLink: router.getRoute('usagepoints/view/processes').buildUrl({usagePointId: mRID}),
                        cancelLink: router.getRoute('usagepoints/view').buildUrl({usagePointId: mRID}),
                        context: {id: usagePoint.get('mRID')}
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    getPrivileges: function () {
        var executionPrivileges = [];

        Dbp.privileges.DeviceProcesses.canExecuteLevel1() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel1.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel2() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel2.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel3() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel3.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel4() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel4.toString()});

        return executionPrivileges;
    }
});
