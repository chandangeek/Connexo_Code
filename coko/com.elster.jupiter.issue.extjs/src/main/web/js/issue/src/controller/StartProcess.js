Ext.define('Isu.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    views: [
        'Isu.view.issues.StartProcess'
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.startprocess.controller.StartProcess'); // Forces registration.
    },

    showStartProcess: function (issueId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Idc.model.Issue').load(issueId, {
            success: function (issue) {
                var widget;

                viewport.setLoading(false);
                widget = Ext.widget('isu-start-process-view', {
                    properties: {
                        activeProcessesParams: {
                            type: 'issue',
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'issue'
                            },
                            {
                                name: 'id',
                                value: 'issueId'
                            },
                            {
                                name: 'value',
                                value: issueId
                            }
                        ],
                        successLink: router.getRoute(router.currentRoute.replace('/startProcess', '')).buildUrl({issueId: issueId}),
                        cancelLink: router.getRoute(router.currentRoute.replace('/startProcess', '')).buildUrl({issueId: issueId})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('issueLoad', issue);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    getPrivileges: function () {
        var executionPrivileges = [];

        Isu.privileges.Issue.canExecuteLevel1() && executionPrivileges.push({privilege: Isu.privileges.Issue.executeLevel1.toString()});
        Isu.privileges.Issue.canExecuteLevel2() && executionPrivileges.push({privilege: Isu.privileges.Issue.executeLevel2.toString()});
        Isu.privileges.Issue.canExecuteLevel3() && executionPrivileges.push({privilege: Isu.privileges.Issue.executeLevel3.toString()});
        Isu.privileges.Issue.canExecuteLevel4() && executionPrivileges.push({privilege: Isu.privileges.Issue.executeLevel4.toString()});

        return executionPrivileges;
    }
});