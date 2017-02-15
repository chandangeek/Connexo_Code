/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.ApplyIssueAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.issues.ActionView',
        'Isu.view.issues.AssignIssue'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueWorkgroupAssignees'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-action-view'
        },
        {
            ref: 'form',
            selector: 'issue-action-view #issue-action-view-form'
        },
        {
            ref: 'assignIssuePage',
            selector: 'assign-issue'
        }
    ],

    actionUrl: '/api/isu/issues/{0}/actions',
    assignUrl: '/api/isu/issues/{0}/{1}',
    init: function () {
        this.control({
            'issue-action-view issue-action-form #issue-action-apply': {
                click: this.applyAction
            },
            'issues-action-menu #assign-to-me': {
                click: this.assignToMe
            },
            'issues-action-menu #unassign': {
                click: this.unassign
            }
        });
    },

    showOverview: function (issueId, actionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            issueType = router.queryParams.issueType,
            actionModel = Ext.create('Isu.model.Issue').actions().model,
            issueModel,
            fromOverview = router.queryParams.fromOverview === 'true',
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 2,
            onAllDependenciesLoad = function () {
                var clipboard,
                    app;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app = me.getApplication();
                    clipboard = Ext.getStore('Isu.store.Clipboard');
                    app.fireEvent('issueActionLoad', actionRecord);
                    app.fireEvent('issueLoad', issueRecord);
                    mainView.setLoading(false);
                    if (actionRecord.properties().getCount() === 0) {
                        me.applyAction(null, null, actionRecord, clipboard.get('issue') || issueRecord);
                        clipboard.clear('issue');
                    } else {
                        var widget = Ext.widget('issue-action-view', {router: router}),
                            form = widget.down('#issue-action-view-form'),
                            cancelLink = form.down('#issue-action-cancel'),
                            queryParamsForCancel = fromOverview ? router.queryParams : null;

                        cancelLink.href = router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).buildUrl(null, queryParamsForCancel);
                        app.fireEvent('changecontentevent', widget);
                        form.loadRecord(actionRecord);
                        form.issue = issueRecord;

                        //todo: this definitely should be refactored. BE should send action button translation instead of this splitting
                        if (form.title === 'Close issue' || form.title === 'Close alarm' || form.title === 'Notify user' || form.title === 'Assign issue' || form.title === 'Assign alarm') {
                            form.down('#issue-action-apply').setText(form.title.split(' ')[0]);
                        }
                    }
                }
            },
            actionRecord,
            issueRecord;


        mainView.setLoading();
        actionModel.getProxy().url = Ext.String.format(me.actionUrl, issueId);
        actionModel.load(actionId, {
            success: function (record) {
                actionRecord = record;
                onAllDependenciesLoad();
            }
        });

        if (issueType == 'datacollection') {
            issueModel = me.getModel('Idc.model.Issue');
        } else if (issueType == 'datavalidation') {
            issueModel = me.getModel('Idv.model.Issue');
        } else {
            issueModel = me.getModel(me.issueModel);
        }
        issueModel.load(issueId, {
            success: function (record) {
                issueRecord = record;
                onAllDependenciesLoad();
            }
        });
    },

    applyAction: function (button, action, actionRecord, issueRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            fromOverview = router.queryParams.fromOverview === 'true',
            queryParamsForBackUrl = fromOverview ? router.queryParams : null,
            backUrl = router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).buildUrl(null, queryParamsForBackUrl),
            record,
            requestOptions;

        requestOptions = {
            backUrl: backUrl,
            success: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText.actions[0].success) {
                    me.getApplication().fireEvent('acknowledge', responseText.actions[0].message);
                    window.location.href = backUrl;
                } else {
                    me.getApplication().getController('Uni.controller.Error').showError(actionRecord.get('issue').title, responseText.actions[0].message);
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (operation.response.status === 400 && responseText.errors && !actionRecord) {
                    errorPanel.show();
                    basicForm.markInvalid(responseText.errors);
                }
                if (operation.response.status === 200 && responseText.actions) {
                    window.location.href = backUrl;
                    me.getApplication().getController('Uni.controller.Error').showError(actionRecord.get('issue').title, responseText.actions[0].message);
                }
            },
            callback: function () {
                mainView.setLoading(false);
            }
        };

        mainView.setLoading();

        if (!!actionRecord) {
            actionRecord.set('issue', _.pick(issueRecord.getData(), 'title', 'version'));
            actionRecord.save(requestOptions);
        } else {
            var form = me.getForm(),
                basicForm = form.getForm(),
                errorPanel = form.down('#issue-action-view-form-errors');

            errorPanel.hide();
            basicForm.clearInvalid();
            form.updateRecord();
            record = form.getRecord();
            record.set('issue', _.pick(form.issue.getData(), 'title', 'version'));
            record.save(requestOptions);
        }
    },

    assignToMe: function (menuItem) {
        this.assign(menuItem, 'assigntome');
    },

    unassign: function (menuItem) {
        this.assign(menuItem, 'unassign');
    },

    assign: function (menuItem, assign) {
        var me = this,
            record = menuItem.record,
            issueId = record.get('id');

        Ext.Ajax.request({
            url: Ext.String.format(me.assignUrl, assign, issueId),
            jsonData: Ext.JSON.encode({'issue' : _.pick(record.getData(), 'title', 'version')}),
            method: 'PUT',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                if (decoded.data && decoded.data.success) {
                    me.getApplication().fireEvent('acknowledge', decoded.data.success[0].title);
                }
                else if (decoded.success) {
                    me.getApplication().fireEvent('acknowledge', decoded.success[0].title);
                }


                var mainView = Ext.ComponentQuery.query('#contentPanel')[0];
                if (mainView && mainView.down('issues-grid')) {
                    var grid = mainView.down('issues-grid');
                    grid.getStore().load();
                }
                else if (mainView && mainView.down('alarms-grid')) {
                    var grid = mainView.down('alarms-grid');
                    grid.getStore().load();
                }
                else {
                    var detail = Ext.ComponentQuery.query('issue-detail-top')[0] ||
                        Ext.ComponentQuery.query('alarm-detail-top')[0];
                    if (detail) {
                        var router = me.getController('Uni.controller.history.Router'),
                            issueType = router.queryParams.issueType,
                            issueModel;

                        if (issueType == 'datacollection') {
                            issueModel = me.getModel('Idc.model.Issue');
                        } else if (issueType == 'datavalidation') {
                            issueModel = me.getModel('Idv.model.Issue');
                        }
                        else {
                            issueModel = me.issueModel;
                        }

                        Ext.ModelManager.getModel(issueModel).load(issueId, {
                            success: function (issue) {
                                if (issueType == 'datacollection') {
                                    Ext.ComponentQuery.query('#data-collection-issue-detail-container')[0].down('form').loadRecord(issue);
                                    Ext.ComponentQuery.query('#issue-detail-action-menu')[0].record = issue;
                                } else if (issueType == 'datavalidation') {
                                    Ext.ComponentQuery.query('#issue-detail-form')[0].loadRecord(issue);
                                    Ext.ComponentQuery.query('#issue-detail-action-menu')[0].record = issue;
                                }
                                else {
                                    Ext.ComponentQuery.query('#alarm-detail-form')[0].loadRecord(issue);
                                    Ext.ComponentQuery.query('#alarm-detail-action-menu')[0].record = issue;
                                }
                            }
                        })
                    }
                }
            }
        });
    }
});