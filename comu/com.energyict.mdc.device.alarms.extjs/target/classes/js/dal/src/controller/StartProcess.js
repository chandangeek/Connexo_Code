Ext.define('Dal.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    views: [
        'Dal.view.StartProcess'
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.startprocess.controller.StartProcess'); // Forces registration.
    },

    showStartProcess: function (alarmId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            fromDetails = router.queryParams.details === 'true',
            queryParamsForBackUrl = fromDetails ? router.queryParams : null;

        viewport.setLoading();

        Ext.ModelManager.getModel('Dal.model.Alarm').load(alarmId, {
            success: function (alarm) {
                viewport.setLoading(false);
                var widget = Ext.widget('alarm-start-process-view', {
                    properties: {
                        activeProcessesParams: {
                            type: 'devicealarm',
                            issueReasons: alarm.data.reason,
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'devicealarm'
                            },
                            {
                                name: 'id',
                                value: 'alarmId'
                            },
                            {
                                name: 'value',
                                value: alarmId
                            }
                        ],
                        successLink: router.getRoute(router.currentRoute.replace(fromDetails ? '/startProcess': '/view/startProcess', '')).buildUrl({alarmId: alarmId}, queryParamsForBackUrl),
                        cancelLink: router.getRoute(router.currentRoute.replace(fromDetails ? '/startProcess': '/view/startProcess', '')).buildUrl({alarmId: alarmId}, queryParamsForBackUrl)
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('issueLoad', alarm);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    getPrivileges: function () {
        var executionPrivileges = [];

        Dal.privileges.Alarm.canExecuteLevel1() && executionPrivileges.push({privilege: Dal.privileges.Alarm.executeLevel1.toString()});
        Dal.privileges.Alarm.canExecuteLevel2() && executionPrivileges.push({privilege: Dal.privileges.Alarm.executeLevel2.toString()});
        Dal.privileges.Alarm.canExecuteLevel3() && executionPrivileges.push({privilege: Dal.privileges.Alarm.executeLevel3.toString()});
        Dal.privileges.Alarm.canExecuteLevel4() && executionPrivileges.push({privilege: Dal.privileges.Alarm.executeLevel4.toString()});

        return executionPrivileges;
    }
});