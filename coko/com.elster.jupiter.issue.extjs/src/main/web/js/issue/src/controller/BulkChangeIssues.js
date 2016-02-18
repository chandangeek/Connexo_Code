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

        widget = Ext.widget('bulk-browse');
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
            params: {sort: ['dueDate', 'modTime'], filter: Ext.encode([{property: 'status', value: ['status.open']}])},
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
        var index = wizard.getActiveItemId();
        this.setBulkActionListActiveItem(wizard);
        this.getBulkNavigation().movePrevStep();
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
        this.getBulkNavigation().moveNextStep();
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
            requestUrl = '/api/isu/issues/' + operation,
            warnIssues = [],
            failedIssues = [],
            params = [],
            allIssues = false;

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
        step5panel.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: (operation === 'assign' ? Uni.I18n.translate('issues.processing.assign', 'ISU', 'Assigning {0} issue(s). Please wait...', (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length)) :  Uni.I18n.translate('issues.processing.close', 'ISU', 'Closing {0} issue(s). Please wait...', (requestData.allIssues ? Uni.I18n.translate('general.all', 'ISU', 'all') : requestData.issues.length)))
            })
        );
        Ext.resumeLayouts(true);

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            params: params,
            jsonData: requestData,
            timeout: 120000,
            success: function (response) {
                var obj = Ext.decode(response.responseText).data,
                    successCount = obj.success.length,
                    warnCount = 0,
                    failedCount = 0,
                    successMessage,
                    warnMessage,
                    failedMessage,
                    warnList = '',
                    failList = '';

                if (!Ext.isEmpty(obj.success)) {
                    switch (operation) {
                        case 'assign':
                            if (successCount > 0) {
                                successMessage =  Uni.I18n.translatePlural('issues.assign.success.result5', successCount, 'ISU',
                                        "There were no issues to assign to ",
                                        "Successfully assigned one issue to ",
                                        "Successfully assigned {0} issues to ");
                                successMessage = '\<h3\>' + successMessage + record.get('assignee').title + '\</h3\>\<br\>'
                            }
                            break;
                        case 'close':
                            if (successCount > 0) {
                                successMessage = Uni.I18n.translatePlural('issues.close.success.result', successCount, 'ISU', '-', '<h3>Successfully closed issue</h3><br>', '<h3>Successfully closed issues</h3><br>');
                            }
                    }
                }

                if (!Ext.isEmpty(obj.failure)) {
                    Ext.each(obj.failure, function (fail) {
                        switch (fail.reason) {
                            case 'Issue doesn\'t exist':
                                warnList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    warnCount += 1;
                                    warnIssues.push(issue.id);
                                    warnList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                warnList += '</ul>';
                                break;
                            default:
                                failList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    failedCount += 1;
                                    failedIssues.push(issue.id);
                                    failList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                failList += '</ul>';
                        }
                    });

                    switch (operation) {
                        case 'assign':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.assign.unable.result', warnCount, 'ISU', '-','<h3>Unable to assign one issue</h3><br>', '<h3>Unable to assign {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.assign.failed.result', failedCount, 'ISU', '-', '<h3>Failed to assign one issue</h3><br>', '<h3>Failed to assign {0} issues</h3><br>') + failList;
                            }
                            break;
                        case 'close':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('issues.close.unable.result', warnCount, 'ISU', '-','<h3>Unable to close one issue</h3><br>', '<h3>Unable to close {0} issues</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('issues.close.failed.result', failedCount, 'ISU', '-', '<h3>Failed to close one issue</h3><br>', '<h3>Failed to close {0} issues</h3><br>') + failList;
                            }
                    }
                }

                step5panel.removeAll(true);

                if (successCount > 0) {
                    var successMsgParams = {
                        type: 'success',
                        msgBody: [
                            {html: successMessage}
                        ],
                        closeBtn: false
                    };
                    if (failedCount == 0) {
                        successMsgParams.btns = [
                            {text: Uni.I18n.translate('general.ok', 'ISU', 'OK'), hnd: function () {
                                step5panel.removeAll(true);
                                Ext.History.back();
                            }}
                        ];
                    }
                    var successPanel = Ext.widget('message-panel', successMsgParams);
                    step5panel.add(successPanel);
                }

                if (warnCount > 0) {
                    var warnMessageParams = {
                        type: 'attention',
                        msgBody: [
                            {html: warnMessage}
                        ],
                        btns: [],
                        closeBtn: false
                    };
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
                            {text: "Retry", hnd: function () {
                                me.fireEvent('retryRequest', wizard, failedIssues);
                            }},
                            {text: "Finish", hnd: function () {
                                Ext.History.back();
                            }}
                        ],
                        closeBtn: false
                    };
                    var failedPanel = Ext.widget('message-panel', failedMessageParams);
                    step5panel.add(failedPanel);
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
                    id: bulkStoreRecord.get('assignee').id,
                    type: bulkStoreRecord.get('assignee').type
                };
                break;
            case 'close':
                requestData.status = bulkStoreRecord.get('status');
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
                view = 'issues-assign-form';
                break;
            case  'close':
                view = 'issues-close-form';
                break;
        }

        widget = Ext.widget(view);

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
        var formPanel = wizard.down('bulk-step3').down('form'),
            form = formPanel.getForm();

        if (form.isValid()) {
            var record = this.getBulkRecord(),
                step4Panel = wizard.down('bulk-step4'),
                operation = record.get('operation'),
                message, widget;

            switch (operation) {
                case 'assign':
                    var activeCombo = formPanel.down('combo[name=assigneeCombo]');
                    record.set('assignee', {
                        id: activeCombo.getValue(),
                        type: "User",
                        title: activeCombo.rawValue
                    });
                    if (!record.get('allIssues')) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.assign.withCount', record.get('issues').length, 'ISU', '-', '<h3>Assign one issue?</h3><br>', '<h3>Assign {0} issues?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.willBeAssigned', 'ISU','The selected issue(s) will be assigned to {0}',[record.get('assignee').title])
                    } else {
                        message = Uni.I18n.translate('issues.allIssues.willBeAssigned.title', 'ISU', '<h3>Assign all issues to {0}?</h3><br>', [record.get('assignee').title])
                        + Uni.I18n.translate('issues.allIssues.willBeAssigned', 'ISU','All issues will be assigned to {0}',[record.get('assignee').title]);
                    }
                    break;

                case 'close':
                    if (!record.get('allIssues')) {
                        message = Uni.I18n.translatePlural('issues.selectedIssues.close.withCount', record.get('issues').length, 'ISU', '-', '<h3>Close one issue?</h3><br>', '<h3>Close {0} issues?</h3><br>')
                        + Uni.I18n.translate('issues.selectedIssues.willBeClosed','ISU', 'The selected issue(s) will be closed with status "<b>{0}</b>"', [record.get('statusName')]);
                    } else {
                        message = Uni.I18n.translate('issues.allIssues.willBeClosed.title', 'ISU', '<h3>Close all issues?</h3><br>')
                        + Uni.I18n.translate('issues.allIssues.willBeClosed', 'ISU', 'All issues will be closed with status "<b>{0}</b>"',[record.get('statusName')]);
                    }
                    break;
            }

            record.set('comment', formPanel.down('textarea').getValue().trim());

            widget = Ext.widget('container', {
                style: 'margin: 20px 0',
                html: message
            });
            Ext.suspendLayouts();
            step4Panel.removeAll(true);
            Ext.resumeLayouts();
            step4Panel.add(widget);
        }
    },

    beforeStep4: function () {
        if (this.getBulkRecord().get('operation') == 'assign') {
            var form = this.getPage().down('bulk-step3 issues-assign-form').getForm();
            return !form || form.isValid();
        }
    }
});