/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.IssueStatuses',
        'Isu.store.UserList',
        'Isu.store.IssuesBuffered',
        'Isu.store.BulkChangeIssues'
    ],

    views: [
        'Isu.view.issues.bulk.Browse',
        'Isu.view.issues.MessagePanel'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'bulk-browse'
        },
        {
            ref: 'bulkNavigation',
            selector: 'bulk-browse bulk-navigation'
        }
    ],

    dataCollectionActivated: false,
    dataValidationActivated: false,

    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

    init: function () {
        this.control({
            'bulk-browse bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            'bulk-browse bulk-navigation': {
                movetostep: this.setActivePage
            },
            'bulk-browse bulk-wizard bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent,
                afterrender: this.getDefaultStep2Operation
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close-form radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            'bulk-browse bulk-wizard bulk-step3 set-priority-form radiogroup': {
                change: this.onStep3RadiogroupSetPriorityChangeEvent,
                afterrender: this.getDefaultSetPriorityStatus
            },
            'bulk-browse bulk-wizard bulk-step3 snooze-bulk-form radiogroup': {
                change: this.onStep3RadiogroupSnoozeChangeEvent,
                afterrender: this.getDefaultSnoozeStatus
            },
            'bulk-browse bulk-step4': {
                beforeactivate: this.beforeStep4
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            }
        });
    },

    issueClosingFormBeforeRenderEvent: function (form) {
        var statusesContainer = form.down('[name=status]'),
            values = Ext.state.Manager.get('formCloseValues');
        Ext.Ajax.request({
            url: '/api/isu/statuses',
            method: 'GET',
            success: function (response) {
                var statuses = Ext.decode(response.responseText).data;
                Ext.each(statuses, function (status) {
                    if (!Ext.isEmpty(status.allowForClosing) && status.allowForClosing) {
                        statusesContainer.add({
                            boxLabel: status.name,
                            inputValue: status.id,
                            name: 'status'
                        })
                    }
                });
                if (Ext.isEmpty(values)) {
                    statusesContainer.items.items[0].setValue(true);
                } else {
                    statusesContainer.down('[inputValue=' + values.status + ']').setValue(true);
                }
            }
        });
        if (values) {
            form.down('textarea').setValue(values.comment);
        }
    },

    showOverview: function () {
        var me = this,
            issuesStore = this.getStore('Isu.store.IssuesBuffered'),
            issuesStoreProxy = issuesStore.getProxy(),
            queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
            filter = [],
            widget, grid;

        issuesStoreProxy.extraParams = {};
        if (queryStringValues.sort) {
            issuesStoreProxy.setExtraParam('sort', queryStringValues.sort);
            delete queryStringValues.sort;
        }
        if (Ext.isDefined(queryStringValues.groupingType) && Ext.isDefined(queryStringValues.groupingValue) && Ext.isEmpty(queryStringValues[queryStringValues.groupingType])) {
            filter.push({
                property: queryStringValues.groupingType,
                value: queryStringValues.groupingValue
            });
        }
        delete queryStringValues.groupingType;
        delete queryStringValues.groupingValue;
        Ext.iterate(queryStringValues, function (name, value) {
            filter.push({
                property: name,
                value: value
            });
        });
        filter.push({
            property: 'application',
            value: Uni.util.Application.getAppName() == 'MdmApp' ? 'INS' :
                Uni.util.Application.getAppName() == 'MultiSense' ? 'MultiSense' : ''
        });
        widget = Ext.widget('bulk-browse');
        widget.down('#Close').setVisible(me.dataCollectionActivated);
        widget.down('#retry-comtask-radio').setVisible(me.dataCollectionActivated);
        widget.down('#retry-comtask-now-radio').setVisible(me.dataCollectionActivated);
        widget.down('#retry-connection-radio').setVisible(me.dataCollectionActivated);
        widget.down('#SetPriority').setVisible(me.dataCollectionActivated);
        widget.down('#Snooze').setVisible(me.dataCollectionActivated);
        grid = widget.down('bulk-step1').down('issues-selection-grid');
        grid.reconfigure(issuesStore);
        grid.filterParams = Ext.clone(filter);

        me.getApplication().fireEvent('changecontentevent', widget);
        issuesStore.data.clear();
        issuesStore.clearFilter(!!filter.length);
        issuesStore.filter(filter);
        issuesStore.on('load', function () {
            grid.onSelectDefaultGroupType();
        }, me, {single: true});
    },

    setActivePage: function (index) {
        var wizard = this.createdWizard;
        wizard.show();
        wizard.activeItemId = index - 1;
        wizard.getLayout().setActiveItem(wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
    },


    setFailedBulkRecordIssues: function (failedIssues) {
        var record = this.getBulkRecord(),
            previousIssues = record.get('issues'),
            leftIssues = [];

        Ext.each(previousIssues, function (issue) {
            if (Ext.Array.contains(failedIssues, issue.get('id'))) {
                leftIssues.push(issue);
            }
        });

        record.set('issues', leftIssues);
        record.commit();
    },

    onIssuesListAfterRender: function (grid) {
        grid.mask();
        grid.store.load({
            params: {sort: ['-priorityTotal'], filter: Ext.encode([{property: 'status', value: ['status.open']}])},
            start: 0,
            limit: 99999,
            callback: function () {
                grid.unmask();
            }
        });
    },

    onBulkActionEvent: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation'),
            isRetry = (operation == 'retrycomm') || (operation == 'retrycommnow') || (operation == 'retryconn');

        if (index == 2 && isRetry) {
            Ext.suspendLayouts();
            wizard.getLayout().setActiveItem(--wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            this.getBulkNavigation().moveToStep(2);
            Ext.resumeLayouts(true);
        } else {
            this.getBulkNavigation().movePrevStep();
        }
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation'),
            isRetry = (operation == 'retrycomm') || (operation == 'retrycommnow') || (operation == 'retryconn');

        if (index == 2 && isRetry) {
            index++;
            Ext.suspendLayouts();
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            this.getBulkNavigation().moveToStep(4);
            Ext.resumeLayouts(true);
        } else {
            this.getBulkNavigation().moveNextStep();
        }
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function (wizard) {
        this.createdWizard = wizard;
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardFinishedEvent: function (wizard) {
        var me = this,
            step5panel = me.getPage().down('bulk-wizard').down('bulk-step5'),
            record = me.getBulkRecord(),
            requestData = me.getRequestData(record),
            operation = record.get('operation'),
            isRetry = (operation === 'retrycomm') || (operation === 'retrycommnow') || (operation === 'retryconn'),
            requestUrl = (operation === 'assign' ||
                operation === 'setpriority' ||
                operation === 'close') ? '/api/isu/issues/' + operation : '/api/idc/issues/' + operation,
            warnIssues = [],
            failedIssues = [],
            params = [],
            allIssues = false,
            step5panelText = '';

        this.setBulkActionListActiveItem(wizard);

        if (record.get('allIssues')) {
            allIssues = record.data.allIssues;
            params = {};
            params.filter = Ext.encode(record.data.params);
        } else {
            params = [];
            allIssues = false;
        }

        var pb = Ext.create('Ext.ProgressBar', {width: '50%'});
        Ext.suspendLayouts();
        step5panel.removeAll(true);
        switch (operation) {
            case 'assign':
                step5panelText = Uni.I18n.translate('issues.processing.assign', 'ISU', 'Assigning {0} issue(s). Please wait...',
                    (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length));
                break;
            case 'close':
                step5panelText = Uni.I18n.translate('issues.processing.close', 'ISU', 'Closing {0} issue(s). Please wait...',
                    (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length));
                break;
            case 'setpriority':
                step5panelText = Uni.I18n.translate('issues.processing.setpriority', 'ISU', 'Setting priority for {0} issue(s). Please wait...',
                    (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length));
                break;
            case 'snooze':
                step5panelText = Uni.I18n.translate('issues.processing.snooze', 'ISU', 'Snooze {0} issue(s). Please wait...',
                    (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length));
                requestUrl = '/api/isu/issues/bulksnooze';
                break;
            default:
                requestUrl = '/api/idc/issues/' + operation;
                step5panelText = Uni.I18n.translate('issues.processing.default', 'ISU', 'Processing {0} issue(s). Please wait...',
                    (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length));
                break;
        }
        step5panel.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: step5panelText
            })
        );
        Ext.resumeLayouts(true);

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            params: params,
            jsonData: requestData,
            timeout: 300000,
            success: function (response) {
                var obj = Ext.decode(response.responseText).data;
                var successCount = obj.success.length;
                var successMessage = '',
                    warnCount = 0,
                    failedCount = 0,
                    warnMessage = '',
                    failedMessage = '',
                    warnList = '',
                    failList = '';
                if (!Ext.isEmpty(obj.success)) {
                    switch (operation) {
                        case 'assign':
                            if (successCount > 0) {
                                var userId = record.get('assignee').userId;
                                var workGroupId = record.get('assignee').workGroupId;

                                if ((userId == -1) && (workGroupId == -1)) {
                                    successMessage = Uni.I18n.translatePlural('issues.assign.success.result51', successCount, 'ISU',
                                        "There were no issues",
                                        "Successfully unassigned one issue",
                                        "Successfully unassigned {0} issues");
                                } else if ((userId > -1) && (workGroupId == -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('issues.assign.success.result520', 'ISU', "There were no issues to assigned to {0} user", record.get('assignee').title) :
                                        (successCount == 1) ? Uni.I18n.translate('issues.assign.success.result521', 'ISU', "Successfully assigned one issue to {0} user", record.get('assignee').title) :
                                            Uni.I18n.translate('issues.assign.success.result522', 'ISU', "Successfully assigned {0} issues to {1} user", [successCount, record.get('assignee').title]);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                } else if ((userId == -1) && (workGroupId > -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('issues.assign.success.result530', 'ISU', "There were no issues to assigned to {0} workgroup", record.get('assignee').workGroupTitle) :
                                        (successCount == 1) ? Uni.I18n.translate('issues.assign.success.result531', 'ISU', "Successfully assigned one issue to {0} workgroup", record.get('assignee').workGroupTitle) :
                                            Uni.I18n.translate('issues.assign.success.result532', 'ISU', "Successfully assigned {0} issues to {1} workgroup", [successCount, record.get('assignee').workGroupTitle]);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                } else if ((userId > -1) && (workGroupId > -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('issues.assign.success.result540', 'ISU', "There were no issues to assigned to {0} user and {1} workgroup", [record.get('assignee').title, record.get('assignee').workGroupTitle]) :
                                        (successCount == 1) ? Uni.I18n.translate('issues.assign.success.result541', 'ISU', "Successfully assigned one issue to {0} user and {1} workgroup", [record.get('assignee').title, record.get('assignee').workGroupTitle]) :
                                            Uni.I18n.translate('issues.assign.success.result542', 'ISU', "Successfully assigned {0} issues to {1} user and {2} workgroup", [successCount, record.get('assignee').title, record.get('assignee').workGroupTitle]);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                }
                            }
                            break;
                        case 'close':
                            if (successCount > 0) {
                                successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.close.success.result', successCount, 'ISU',
                                    "-",
                                    "Successfully closed one issue",
                                    "Successfully closed {0} issues") + '\</h3\>\<br\>';
                            }
                            break;
                        case 'retrycomm':
                            if (successCount > 0) {
                                successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.retrycomm.success.result', successCount, 'ISU',
                                    "No communication tasks have been retriggered",
                                    "Communication tasks have been retriggered for {0} issue",
                                    "Communication tasks have been retriggered for {0} issues") + '\</h3\>\<br\>';
                            }
                            break;
                        case 'retrycommnow':
                            if (successCount > 0) {
                                successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.retrycomm.success.result', successCount, 'ISU',
                                    "No communication tasks have been retriggered",
                                    "Communication tasks have been retriggered for {0} issue",
                                    "Communication tasks have been retriggered for {0} issues") + '\</h3\>\<br\>';
                            }
                            break;
                        case 'retryconn':
                            if (successCount > 0) {
                                successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.retryconn.success.result', successCount, 'ISU',
                                    "No connections have been retriggered",
                                    "Connections have been retriggered for {0} issue",
                                    "Connections have been retriggered for {0} issues") + '\</h3\>\<br\>';
                            }
                            break;

                        case 'setpriority':
                            if (successCount > 0) {
                                if (record.get('allIssues')) {
                                    successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.setpriority.successAllIssues.result', successCount, 'ISU',
                                        "-",
                                        "Successfully set priority for {0} issue",
                                        "Successfully set priority for {0} issues") + '\</h3\>\<br\>';
                                } else {
                                    successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.setpriority.successSelectedIssues.result', successCount, 'ISU',
                                        "-",
                                        "Successfully set priority for {0} selected issue",
                                        "Successfully set priority for {0} selected issues") + '\</h3\>\<br\>';
                                }

                            }
                            break;
                        case 'snooze':
                            if (successCount > 0) {
                                if (record.get('allIssues')) {
                                    if (Ext.isEmpty(obj.failure)) {
                                        successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.snooze.successAllIssuesAll.result', successCount, 'ISU',
                                            "-",
                                            "Successfully snoozed {0} issue",
                                            "Successfully snoozed all issues") + '\</h3\>\<br\>';
                                    } else {
                                        successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.snooze.successAllIssuesPartial.result', successCount, 'ISU',
                                            "-",
                                            "Successfully snoozed {0} issue",
                                            "Successfully snoozed {0} issues") + '\</h3\>\<br\>';
                                    }
                                } else {
                                    if (Ext.isEmpty(obj.failure)) {
                                        successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.snooze.successSelectedIssuesAll.result', successCount, 'ISU',
                                            "-",
                                            "Successfully snoozed {0} selected issue",
                                            "Successfully snoozed all selected issues") + '\</h3\>\<br\>';
                                    } else {
                                        successMessage = '\<h3\>' + Uni.I18n.translatePlural('issues.snooze.successSelectedIssuesPartial.result', successCount, 'ISU',
                                            "-",
                                            "Successfully snoozed {0} selected issue",
                                            "Successfully snoozed {0} selected issues") + '\</h3\>\<br\>';
                                    }
                                }

                            }
                            break;
                    }
                }

                if (!Ext.isEmpty(obj.failure)) {
                    if (isRetry) {
                        warnCount = obj.failure[0].issues.length;
                        warnList = '<h4>' + obj.failure[0].reason + '</h4><br>';
                    } else {

                        Ext.each(obj.failure, function (fail) {
                            switch (fail.reason) {
                                case 'Issue doesn\'t exist':
                                    warnList += '<h3>' + fail.reason + ':</h3><ul style="list-style: none; padding-left: 1em;">';
                                    Ext.each(fail.issues, function (issue) {
                                        var issueType = me.getIssueType(obj.failure[0].issues, issue.id);
                                        warnCount += 1;
                                        warnIssues.push(issue.id);
                                        failList += '<li>- <a href="#/workspace/issues/' + issue.id + '?issueType=' + issueType + '">' + issue.title + '</a></li>';
                                    });
                                    warnList += '</ul>';
                                    break;
                                default:
                                    failList += '<h3>' + fail.reason + ':</h3><ul style="list-style: none; padding-left: 1em;">';
                                    Ext.each(fail.issues, function (issue) {
                                        var issueType = me.getIssueType(obj.failure[0].issues, issue.id);
                                        failedCount += 1;
                                        failedIssues.push(issue.id);
                                        failList += '<li>- <a href="#/workspace/issues/' + issue.id + '?issueType=' + issueType + '">' + issue.title + '</a></li>';
                                    });
                                    failList += '</ul>';
                            }
                        });
                    }

                    switch (operation) {
                        case 'assign':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.assign.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to assign one issue</h3><br>', '<h3 style="color: #eb5642">Unable to assign {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.assign.failed.results', failedCount, 'ISU', '-', '<h3 style="color: #eb5642">Failed to assign one issue</h3><br>', '<h3 style="color: #eb5642">Failed to assign {0} issues</h3><br>') + failList;
                            }
                            break;
                        case 'close':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.close.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to close one issue</h3><br>', '<h3 style="color: #eb5642">Unable to close {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.close.failed.results', failedCount, 'ISU', '-', '<h3 style="color: #eb5642">Failed to close one issue</h3><br>', '<h3 style="color: #eb5642">Failed to close {0} issues</h3><br>') + failList;
                            }
                            break;
                        case 'retrycomm':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.retrycomm.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to retry communication tasks for {0} issue</h3><br>',
                                    '<h3>Unable to retry communication tasks for {0} issues</h3><br>') + warnList;
                            }
                            break;
                        case 'retrycommnow':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.retrycomm.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to retry communication tasks for {0} issue</h3><br>',
                                    '<h3>Unable to retry communication tasks for {0} issues</h3><br>') + warnList;
                            }
                            break;
                        case 'retryconn':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.retryconn.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to retry connections for {0} issue</h3><br>',
                                    '<h3>Unable to retry connections for {0} issues</h3><br>') + warnList;
                            }
                            break;
                        case 'setpriority':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.setpriority.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to set priority for one issue</h3><br>', '<h3 style="color: #eb5642">Unable to set priority for {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.setpriority.failed.results', failedCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to set priority for one issue</h3><br>', '<h3 style="color: #eb5642">Unable to set priority for {0} issues</h3><br>') + failList;
                            }
                            break;
                        case 'snooze':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.snooze.unable.results', warnCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to snooze one issue</h3><br>', '<h3 style="color: #eb5642">Unable to snooze {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.snooze.failed.results', failedCount, 'ISU', '-', '<h3 style="color: #eb5642">Unable to snooze one issue</h3><br>', '<h3 style="color: #eb5642">Unable to snooze for {0} issues</h3><br>') + failList;
                            }
                            break;
                    }
                }

                step5panel.removeAll(true);

                if (response.status === 202) {
                    var msg = '\<h3\>' + Uni.I18n.translate('issue.background.message', 'ISU', 'Closing has started and will continue in the background') + '\</h3\>\<br\>';
                    var messageParams = {
                        type: 'success',
                        msgBody: [
                            {html: msg}
                        ],
                        closeBtn: false
                    };
                    messageParams.btns = [
                        {
                            itemId: 'btn-finish',
                            text: Uni.I18n.translate('general.finish', 'ISU', 'Finish'), ui: 'action', hnd: function () {
                                step5panel.removeAll(true);
                                Ext.History.back();
                            }
                        }
                    ];
                    var messagePanel = Ext.widget('message-panel', messageParams);
                    step5panel.add(messagePanel);
                }
                if (warnCount > 0) {
                    var warnMessageParams = {
                        type: 'attention',
                        msgBody: [
                            {html: warnMessage}
                        ],
                        closeBtn: false
                    };
                    if (isRetry) {
                        warnMessageParams.btns = [{
                            itemId: 'btn-finish',
                            text: Uni.I18n.translate('general.finish', 'ISU', 'Finish'),
                            hnd: function () {
                                Ext.History.back();
                            }
                        }];
                    }
                    var warnPanel = Ext.widget('message-panel', warnMessageParams);
                    step5panel.add(warnPanel);
                }

                if (failedCount > 0) {
                    var failedMessageParams = {
                        type: 'error',
                        msgBody: [
                            {html: failedMessage}
                        ],
                        btns: [
                            {
                                text: "Retry", hnd: function () {
                                    me.fireEvent('retryRequest', wizard, failedIssues);
                                }
                            },
                            {
                                text: "Finish", hnd: function () {
                                    Ext.History.back();
                                }
                            }
                        ],
                        closeBtn: false
                    };
                    var failedPanel = Ext.widget('message-panel', failedMessageParams);
                    step5panel.add(failedPanel);
                }

                if (successCount > 0) {
                    var successMsgParams = {
                        type: 'success',
                        msgBody: [
                            {html: successMessage}
                        ],
                        closeBtn: false
                    };
                    if ((!isRetry && failedCount == 0) || (isRetry && warnCount == 0)) {
                        successMsgParams.btns = [
                            {
                                itemId: 'btn-finish',
                                text: Uni.I18n.translate('general.finish', 'ISU', 'Finish'), ui: 'action', hnd: function () {
                                    step5panel.removeAll(true);
                                    Ext.History.back();
                                }
                            }
                        ];
                    }
                    var successPanel = Ext.widget('message-panel', successMsgParams);
                    step5panel.add(successPanel);
                }
                me.getBulkNavigation().moveNextStep();
            },
            failure: function (response) {
                step5panel.removeAll(true);
                Ext.History.back();
            }
        });
    },

    getRequestData: function (bulkStoreRecord) {
        var requestData = {issues: []},
            operation = bulkStoreRecord.get('operation'),
            issues = bulkStoreRecord.get('issues'),
            allIssues = bulkStoreRecord.get('allIssues'),
            params = bulkStoreRecord.get('params');

        if (!allIssues) {
            Ext.iterate(issues, function (issue) {
                requestData.issues.push(
                    {
                        id: issue.get('id'),
                        version: issue.get('version')
                    }
                );
            });
            requestData.params = [];
            requestData.allIssues = false;
        } else {
            requestData.params = params;
            requestData.allIssues = allIssues;
            requestData.issues = [];
        }

        switch (operation) {
            case 'assign':
                requestData.assignee = {
                    userId: bulkStoreRecord.get('assignee').userId,
                    workGroupId: bulkStoreRecord.get('assignee').workGroupId
                };
                break;
            case 'close':
                requestData.status = bulkStoreRecord.get('status');
                break;
            case 'setpriority' :
                requestData.priority = bulkStoreRecord.get('priority');
                break;
            case 'snooze' :
                requestData.snoozeDateTime = bulkStoreRecord.get('snooze').getTime();
                break;
        }

        requestData.comment = bulkStoreRecord.get('comment');

        return requestData;
    },

    onWizardCancelledEvent: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/issues').forward();
    },

    setBulkActionListActiveItem: function (wizard) {
        var index = wizard.getActiveItemId();
    },

    processStep: function (func, wizard) {
        if (func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function () {
        var bulkStore = Ext.getStore('Isu.store.BulkChangeIssues'),
            bulkRecord = bulkStore.getAt(0);

        if (!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }

        return bulkStore.getAt(0);
    },

    onStep2RadiogroupChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('operation', newValue.operation);
        record.commit();
    },

    onStep3RadiogroupCloseChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.set('statusName', radiogroup.getChecked()[0].boxLabel);
        record.commit();
    },

    onStep3RadiogroupSetPriorityChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.set('statusName', radiogroup.getChecked()[0].boxLabel);
        record.commit();
    },

    onStep3RadiogroupSnoozeChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.set('statusName', radiogroup.getChecked()[0].boxLabel);
        record.commit();
    },

    getDefaultStep2Operation: function () {
        var formPanel = this.getPage().down('bulk-wizard').down('bulk-step2').down('panel'),
            default_operation = formPanel.down('radiogroup').getValue().operation,
            record = this.getBulkRecord();
        record.set('operation', default_operation);
        record.commit();
    },

    getDefaultCloseStatus: function () {
        var formPanel = this.getPage().down('bulk-wizard').down('bulk-step3').down('issues-close-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    getDefaultSetPriorityStatus: function () {
        var formPanel = this.getPage().down('bulk-wizard').down('bulk-step3').down('set-priority-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    getDefaultSnoozeStatus: function () {
        var formPanel = this.getPage().down('bulk-wizard').down('bulk-step3').down('snooze-bulk-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    processNextOnStep1: function (wizard) {
        var record = this.getBulkRecord(),
            grid = wizard.down('bulk-step1').down('issues-selection-grid'),
            selection = grid.getSelectionModel().getSelection();

        if (grid.isAllSelected()) {
            var allIssues = true;
            var params = Ext.ComponentQuery.query('grid')[0].filterParams;
            record.set('allIssues', allIssues);
            record.set('params', params);
            record.set('issues', []);
        } else {
            record.set('issues', selection);
            record.set('allIssues', false);
            record.set('params', []);
        }

        record.commit();
    },

    processNextOnStep2: function (wizard) {
        var record = this.getBulkRecord(),
            step3Panel = wizard.down('bulk-step3'),
            operation = record.get('operation'),
            view,
            widget;

        switch (operation) {
            case 'assign':
                view = 'assign-issue';
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
            case  'close':
                view = 'issues-close-form';
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
            case 'setpriority':
                view = 'set-priority-form';
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
            case 'snooze':
                view = 'snooze-bulk-form';
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
        }

        if (operation == 'setpriority') {
            widget.down('#savePriority').setVisible(false);
            widget.down('#cancel').setVisible(false);
            widget.down('#num-urgency').setValue(25);
            widget.down('#num-impact').setValue(5);
        }

        if (operation == 'assign') {
            widget.down('#frm-assign-issue').setTitle('');

            widget.down('#issue-assign-action-apply').setVisible(false);
            widget.down('#issue-assign-action-cancel').setVisible(false);
            widget.down('#cbo-workgroup-issue-assignee').setValue(-1);
            widget.down('#cbo-user-issue-assignee').setValue(-1);
        }
        if (!Ext.isEmpty(widget.items.getAt(1))) {
            widget.items.getAt(1).margin = '0';
        }

        if (widget) {
            Ext.suspendLayouts();
            step3Panel.removeAll(true);
            Ext.resumeLayouts();
            step3Panel.add(widget);
        }
    },

    processNextOnStep3: function (wizard) {
        var record = this.getBulkRecord(),
            step4Panel = wizard.down('bulk-step4'),
            operation = record.get('operation'),
            formPanel = wizard.down('bulk-step3').down('form'),
            message, widget;

        switch (operation) {
            case 'assign':
                var userCombo = formPanel.down('#cbo-user-issue-assignee');
                var workGroupCombo = formPanel.down('#cbo-workgroup-issue-assignee');
                var userId = userCombo.getValue();
                var workGroupId = workGroupCombo.getValue();
                record.set('assignee', {
                    userId: userCombo.getValue(),
                    workGroupId: workGroupCombo.getValue(),
                    title: userCombo.rawValue,
                    workGroupTitle: Ext.String.htmlEncode(workGroupCombo.rawValue)
                });
                if (!record.get('allIssues')) {
                    if ((userId == -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.assign.msg1', record.get('issues').length, 'ISU', '-', '<h3>Unassign one issue?</h3><br>', '<h3>Unassign {0} issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title1', 'ISU', 'The selected issue(s) will be unassigned');
                    } else if ((userId > -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.assign.msg2', record.get('issues').length, 'ISU', '-', '<h3>Assign one issue?</h3><br>', '<h3>Assign {0} issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title2', 'ISU', 'The selected issue(s) will be assigned to {0} user', [Ext.String.htmlEncode(userCombo.rawValue)])
                    } else if ((userId == -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.assign.msg2', record.get('issues').length, 'ISU', '-', '<h3>Assign one issue?</h3><br>', '<h3>Assign {0} issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title3', 'ISU', 'The selected issue(s) will be assigned to {0} workgroup', [Ext.String.htmlEncode(workGroupCombo.rawValue)])
                    } else if ((userId > -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.assign.msg2', record.get('issues').length, 'ISU', '-', '<h3>Assign one issue?</h3><br>', '<h3>Assign {0} issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title4', 'ISU', 'The selected issue(s) will be assigned to {0} workgroup and {1} user', [Ext.String.htmlEncode(workGroupCombo.rawValue), Ext.String.htmlEncode(userCombo.rawValue)])
                    }
                } else {
                    if ((userId == -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translate('issues.selectedIssues.assign.msg3', 'ISU', '<h3>Unassign all issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title5', 'ISU', 'All issues will be unassigned');
                    } else if ((userId > -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translate('issues.selectedIssues.assign.msg4', 'ISU', '<h3>Assign all issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title6', 'ISU', 'All issues will be assigned to {0} user', [Ext.String.htmlEncode(userCombo.rawValue)])
                    } else if ((userId == -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translate('issues.selectedIssues.assign.msg4', 'ISU', '<h3>Assign all issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title7', 'ISU', 'All issues will be assigned to {0} workgroup', [Ext.String.htmlEncode(workGroupCombo.rawValue)])
                    } else if ((userId > -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translate('issues.selectedIssues.assign.msg4', 'ISU', '<h3>Assign all issues?</h3><br>')
                            + Uni.I18n.translate('issues.selectedIssues.assign.title8', 'ISU', 'All issues will be assigned to {0} workgroup and {1} user', [Ext.String.htmlEncode(workGroupCombo.rawValue), Ext.String.htmlEncode(userCombo.rawValue)])
                    }
                }
                break;

            case 'close':
                if (!record.get('allIssues')) {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.close.withCount', record.get('issues').length, 'ISU', '-', '<h3>Close one issue?</h3><br>', '<h3>Close {0} issues?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.willBeClosed', 'ISU', 'The selected issue(s) will be closed with status "<b>{0}</b>"', [record.get('statusName')]);
                } else {
                    message = Uni.I18n.translate('issues.allIssues.willBeClosed.title', 'ISU', '<h3>Close all issues?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.willBeClosed', 'ISU', 'All issues will be closed with status "<b>{0}</b>"', [record.get('statusName')]);
                }
                break;

            case 'retrycomm':
                if (!record.get('allIssues')) {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.retryCommunication.withCount', record.get('issues').length, 'ISU', '-', '<h3>Retry one communication task?</h3><br>', '<h3>Retry {0} communication tasks?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.retryCommunication.willBeRetriggered', 'ISU', 'Communication tasks of selected issue(s) will be retriggered');
                } else {
                    message = Uni.I18n.translate('issues.allIssues.retryCommunication.title', 'ISU', '<h3>Retry all communication tasks?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.retryCommunication', 'ISU', 'Communication tasks of all issues will be retriggered');
                }
                break;

            case 'retrycommnow':
                if (!record.get('allIssues')) {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.retryCommunication.withCount', record.get('issues').length, 'ISU', '-', '<h3>Retry one communication task?</h3><br>', '<h3>Retry {0} communication tasks?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.retryCommunication.willBeRetriggered', 'ISU', 'Communication tasks of selected issue(s) will be retriggered');
                } else {
                    message = Uni.I18n.translate('issues.allIssues.retryCommunication.title', 'ISU', '<h3>Retry all communication tasks?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.retryCommunication', 'ISU', 'Communication tasks of all issues will be retriggered');
                }
                break;

            case 'retryconn':
                if (!record.get('allIssues')) {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.retryConnection.withCount', record.get('issues').length, 'ISU', '-', '<h3>Retry one connection?</h3><br>', '<h3>Retry {0} connections?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.retryConnection.willBeRetriggered', 'ISU', 'Connections of selected issue(s) will be retriggered');
                } else {
                    message = Uni.I18n.translate('issues.allIssues.retryConnection.title', 'ISU', '<h3>Retry all connections?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.retryConnection', 'ISU', 'Connections of all issues will be retriggered');
                }
                break;
            case 'setpriority':
                record.set('priority', formPanel.down("#num-urgency").getValue() + ":" + formPanel.down("#num-impact").getValue());
                if (record.get('allIssues')) {
                    message = Uni.I18n.translate('issues.allIssues.setPriority.title', 'ISU', '<h3>Set priority for all issues?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.setPriority', 'ISU', 'All issues will have the priority set to {0}', [formPanel.down("#priority-label").text]);
                } else {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.setPriority.withCount', record.get('issues').length, 'ISU', '-', '<h3>Set priority for one issue?</h3><br>', '<h3>Set priority for {0} issues?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.setPriority', 'ISU', 'The priority of the selected issue(s) will be set to {0}', [formPanel.down("#priority-label").text]);
                }
                break;
            case 'snooze':
                record.set('snooze', formPanel.down("#issue-snooze-until-date").getValue());
                if (record.get('allIssues')) {
                    message = Uni.I18n.translate('issues.allIssues.snooze.title', 'ISU', '<h3>Snooze all issues?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.snooze', 'ISU', 'All issues will be snoozed ');
                } else {
                    message = Uni.I18n.translatePlural('issues.selectedIssues.snooze.withCount', record.get('issues').length, 'ISU', '-', '<h3>Snooze one issue?</h3><br>', '<h3>Snooze {0} issues?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.snooze', 'ISU', 'The selected issue(s) will be snoozed ');
                }
        }

        if (formPanel && (operation != 'setpriority') && (operation != 'snooze')) {
            record.set('comment', formPanel.down('textarea').getValue().trim());
        }

        widget = Ext.widget('container', {
            style: 'margin: 20px 0',
            html: message
        });
        Ext.suspendLayouts();
        step4Panel.removeAll(true);
        Ext.resumeLayouts();
        step4Panel.add(widget);
    },

    beforeStep4: function () {
        return true;
    },

    getIssueType: function (array, value) {
        for (var i = 0; i < array.length; i++) {
            if (array[i].id === value && array[i].title.indexOf('estimate') >= 0) {
                return "datavalidation";
            }
        }
        return "datacollection";
    }

});