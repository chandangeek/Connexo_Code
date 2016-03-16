Ext.define('Imt.processes.controller.MonitorProcesses', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    stores: [
    ],
    views: [
        'Imt.processes.view.UsagePointProcessesMainView',
        'Imt.processes.view.UsagePointStartProcess'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'}
    ],

    showUsagePointProcesses: function (usagePointId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-main-view', {
                    router: router,
                    mRID: usagepoint.get('mRID'),
                    properties: {
                        variableId: 'usagePointId',
                        name: 'usagePoint',
                        value: usagePointId,
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/performTask': null
                    }
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.getOverviewLink().setText(usagepoint.get('mRID'));

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointStartProcess: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', record);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-start', {
                    mRID: mRID,
                    router: router,
                    properties: {
                        activeProcessesParams: {
                            type: 'usagePoint',
                            privileges: Ext.encode(me.getPrivileges())
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
                                value: mRID
                            }
                        ],
                        successLink: router.getRoute('usagepoints/view/processes').buildUrl({usagePointId: mRID}),
                        cancelLink: router.getRoute('usagepoints/view').buildUrl({usagePointId: mRID})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);

            },
            failure: function (response) {
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
