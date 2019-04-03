/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    views: [
        'Itk.view.StartProcess'
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.startprocess.controller.StartProcess'); // Forces registration.
    },

    showStartProcess: function (issueId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            fromDetails = router.queryParams.details === 'true',
            queryParamsForBackUrl = fromDetails ? router.queryParams : null;

        viewport.setLoading();

        Ext.ModelManager.getModel('Itk.model.Issue').load(issueId, {
            success: function (issue) {
                viewport.setLoading(false);
                var widget = Ext.widget('issue-start-process-view', {
                    properties: {
                        activeProcessesParams: {
                            type: 'deviceissue',
                            issueReasons: issue.data.reason,
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        device: issue.get('device'),
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'deviceissue'
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
                        successLink: router.getRoute(router.currentRoute.replace('/startProcess', '')).buildUrl({issueId: issueId}, queryParamsForBackUrl),
                        cancelLink: router.getRoute(router.currentRoute.replace(fromDetails ? '/startProcess' : '/view/startProcess', '')).buildUrl({issueId: issueId}, queryParamsForBackUrl)
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

        Itk.privileges.Issue.canExecuteLevel1() && executionPrivileges.push({privilege: Itk.privileges.Issue.executeLevel1.toString()});
        Itk.privileges.Issue.canExecuteLevel2() && executionPrivileges.push({privilege: Itk.privileges.Issue.executeLevel2.toString()});
        Itk.privileges.Issue.canExecuteLevel3() && executionPrivileges.push({privilege: Itk.privileges.Issue.executeLevel3.toString()});
        Itk.privileges.Issue.canExecuteLevel4() && executionPrivileges.push({privilege: Itk.privileges.Issue.executeLevel4.toString()});

        return executionPrivileges;
    }
});