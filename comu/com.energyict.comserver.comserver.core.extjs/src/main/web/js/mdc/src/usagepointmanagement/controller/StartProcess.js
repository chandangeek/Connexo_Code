Ext.define('Mdc.usagepointmanagement.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    views: [
        'Mdc.usagepointmanagement.view.StartProcess'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'}
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.startprocess.controller.StartProcess'); // Forces registration.
    },

    showStartProcess: function (id) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(id, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);

                viewport.setLoading(false);
                widget = Ext.widget('mdc-usage-point-start-process-view', {
                    router: me.getController('Uni.controller.history.Router'),
                    mRID: usagepoint,
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
                                value: id
                            }
                        ],
                        successLink: router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: id}),
                        cancelLink: router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: id})
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

    getPrivileges: function () {
        var executionPrivileges = [];

        Dbp.privileges.DeviceProcesses.canExecuteLevel1() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel1.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel2() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel2.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel3() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel3.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel4() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel4.toString()});

        return executionPrivileges;
    }
});