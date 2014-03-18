Ext.define('Isu.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.BulkChangeIssues'
    ],

    views: [
        'workspace.issues.Filter',
        'workspace.issues.List',
        'workspace.issues.IssueNoGroup',
        'workspace.issues.bulk.Browse',
        'ext.button.IssuesGridAction',
        'ext.button.SortItemButton'
    ],

    refs: [
        {
            ref: 'bulkActionsList',
            selector: 'bulk-browse bulk-navigation'
        }
    ],

    listeners: {
       retryRequest: function(wizard, failedItems){
           this.setFailedBulkRecordIssues(failedItems);
           this.onWizardFinishedEvent(wizard);
       }
    },

    init: function () {
        this.control({
            'bulk-browse breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'bulk-browse bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            'bulk-browse bulk-wizard bulk-step1 issues-list': {
                afterrender: this.onIssuesListAfterRender,
                selectionchange: this.onIssueListViewSelectAndDeselect
            },
            'bulk-browse bulk-wizard bulk-step1 radiogroup': {
                change: this.onStep1RadiogroupChangeEvent
            },
            'bulk-browse bulk-wizard bulk-step1 button[name=uncheck-all-btn]': {
                click: this.onUncheckAllIssuesEvent
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
            }
        });
    },

    setFailedBulkRecordIssues: function(failedIssues){
        var record = this.getBulkRecord(),
            previousIssues = record.get('issues'),
            leftIssues = [];
        Ext.each (previousIssues, function (issue) {
            if(Ext.Array.contains(failedIssues, issue.get('id'))){
                leftIssues.push(issue);
            }
        });
        record.set('issues', leftIssues);
        record.commit();
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issues',
                href: 'issues'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Bulk action',
                href: 'bulkaction'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onIssuesListAfterRender: function (grid) {
        var self = this,
            step1RadioGroup = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step1').down('radiogroup'),
            step1SelectedIssuesTxtHolder = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step1').down('[name=selected-issues-txt-holder]');
        grid.store.extraParams = {pageSize: 99999};
        step1RadioGroup.mask();
        step1SelectedIssuesTxtHolder.mask();
        grid.mask();
        grid.store.load({
            start: 0,
            limit: 99999,
            callback: function () {
                step1RadioGroup.query('[inputValue=ALL]')[0].setValue(true);
                step1RadioGroup.unmask();
                step1SelectedIssuesTxtHolder.unmask();
                grid.unmask();
            }
        });
        grid.view.selectedItemCls += ' isu-issues-list-selected-no-highlighted';
        grid.view.preserveScrollOnRefresh = true;
    },

    onIssueListViewSelectAndDeselect: function (grid) {
        grid.view.refresh();
        this.step1SelectedIssuesTxtHolderUptade(grid);

        var step1RadioGroup = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step1').down('radiogroup'),
            step1ErrorPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step1').down('[name=step1-errors]');

        if (grid.view.getSelectionModel().getSelection().length > 0) {
            step1RadioGroup.query('[inputValue=SELECTED]')[0].setBoxLabel('<b>Selected issues</b><br/><span style="color: gray;">Select issues in table</span>')
            step1ErrorPanel.setVisible(false);
        }
    },

    onUncheckAllIssuesEvent: function (btn) {
        var grid = btn.up('panel').down('issues-list'),
            radioGroup = btn.up('panel').down('radiogroup');
        grid.getSelectionModel().setLocked(false);
        grid.getSelectionModel().deselectAll(true);
        radioGroup.query('[inputValue=SELECTED]')[0].setValue(true);
        grid.fireEvent('selectionchange', grid);
        grid.view.refresh();
    },

    onBulkActionEvent: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setHyperLinkAction(index, false);
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setHyperLinkAction(index - 1, true);
        this.setBulkActionListActiveItem(wizard);

        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function (wizard) {
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardFinishedEvent: function (wizard) {
        var self = this;
        this.setBulkActionListActiveItem(wizard);
        this.getBulkActionsList().clearHyperLinkActions();
        var step5panel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step5'),
            record = this.getBulkRecord(),
            requestData = this.getRequestData(record),
            operation = record.get('operation'),
            requestUrl = '/api/isu/issue/' + operation,
            failedIssues = [];

        var pb = Ext.create('Ext.ProgressBar', {width: '50%'});
        step5panel.removeAll(true);
        step5panel.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: (operation === 'assign' ? 'Assigning ' : 'Closing ') + requestData.issues.length + ' issue(s). Please wait...'
            })
        );

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            jsonData: requestData,
            success: function (response) {
                var obj = Ext.decode(response.responseText).data,
                    successCount = obj.success.length,
                    failedCount = 0,
                    successMessage,
                    failedMessage,
                    failList = '';

                Ext.each (obj.failure, function (fails) {
                    failList += '<h4>' + fails.reason +':</h4><br>';
                    Ext.each (fails.issues, function(issue){
                        failedCount += 1;
                        failedIssues.push(issue.id);
                        failList += issue.title + '<br>';
                    });
                });

                switch (operation) {
                    case 'assign':
                        if (successCount > 0) {
                            successMessage = '<h3>Successfully assigned ' + successCount + ' issue(s) to ' + record.get('assignee').title + '</h3><br>';
                        }
                        if (failedCount > 0) {
                            failedMessage = '<h3>Failed to assign ' + failedCount + 'issue(s)</h3><br>'
                        }
                        break;
                    case 'close':
                        if (successCount > 0) {
                            successMessage = '<h3>Successfully closed ' + successCount + ' issue(s)</h3><br>';
                        }
                        if (failedCount > 0) {
                            failedMessage = '<h3>Failed to close ' + failedCount + ' issue(s)</h3><br>' + failList;
                        }
                        break;
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
                            {text: "OK", hnd: function () {
                                Ext.History.back();
                            }}
                        ];
                    }
                    successPanel = Ext.widget('message-panel', successMsgParams);
                    successPanel.addClass('isu-bulk-message-panel');
                    step5panel.add(successPanel);
                }

                if (failedCount > 0) {
                    var failedMessageParams = {
                        type: 'error',
                        msgBody: [
                            {html: failedMessage}
                        ],
                        btns: [
                            {text: "Retry", hnd: function () {
                                self.fireEvent('retryRequest', wizard, failedIssues);
                            }},
                            {text: "Finish", hnd: function () {
                                Ext.History.back();
                            }}
                        ],
                        closeBtn: false
                    };
                    failedPanel = Ext.widget('message-panel', failedMessageParams);
                    failedPanel.addClass('isu-bulk-message-panel');
                    step5panel.add(failedPanel);
                }
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
            issues = bulkStoreRecord.get('issues');

        Ext.iterate(issues, function (issue) {
            requestData.issues.push(
                {
                    id: issue.get('id'),
                    version: issue.get('version')
                }
            );
        });

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

    onWizardCancelledEvent: function (wizard) {
        Ext.History.back();
    },

    setBulkActionListActiveItem: function (wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setActiveAction(index);
    },

    processStep: function (func, wizard) {
        if (func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function () {
        var bulkStore = Ext.getStore('Isu.store.BulkChangeIssues');
        var bulkRecord = bulkStore.getAt(0);
        if (!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }
        return bulkStore.getAt(0);
    },

    onStep1RadiogroupChangeEvent: function (radiogroup, newValue, oldValue) {
        var grid = radiogroup.up('panel').down('issues-list');
        switch (newValue.issuesRange) {
            case 'ALL':
                grid.getSelectionModel().selectAll(true);
                grid.getSelectionModel().setLocked(true);
                grid.fireEvent('selectionchange', grid);
                break;
            case 'SELECTED':
                grid.getSelectionModel().setLocked(false);
        }
    },

    onStep2RadiogroupChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('operation', newValue.operation);
        record.commit();
    },

    onStep3RadiogroupCloseChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.commit();
    },

    getDefaultStep2Operation: function () {
        var formPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step2').down('panel'),
            default_operation = formPanel.down('radiogroup').getValue().operation,
            record = this.getBulkRecord();
        record.set('operation', default_operation);
        record.commit();
    },

    getDefaultCloseStatus: function () {
        var formPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step3').down('issues-close-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    processNextOnStep1: function (wizard) {
        var record = this.getBulkRecord(),
            grid = wizard.down('bulk-step1').down('issues-list');

        record.set('issues', grid.getSelectionModel().getSelection());
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
            step3Panel.removeAll(true);
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
                    var activeRadio = formPanel.down('radiogroup').down('radio[checked=true]').inputValue,
                        activeCombo = formPanel.down('combo[name=' + activeRadio + ']');
                    record.set('assignee', {
                        id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                        type: activeCombo.name,
                        title: activeCombo.rawValue
                    });
                    message = '<h3>Assign ' + record.get('issues').length + ' issue(s) to ' + record.get('assignee').title + '?</h3><br>'
                        + 'The selected issue(s) will be assigned to ' + record.get('assignee').title;
                    break;

                case 'close':
                    message = '<h3>Close ' + record.get('issues').length + ' issue(s)?</h3><br>'
                        + 'The selected issue(s) will be closed with status <b>' + record.get('status') + '</b>';
                    break;
            }

            record.set('comment', formPanel.down('textarea').getValue().trim());

            widget = Ext.widget('container', {
                cls: 'isu-bulk-assign-confirmation-request-panel',
                html: message
            });

            step4Panel.removeAll(true);
            step4Panel.add(widget);
        }
    },

    beforeStep4: function () {
        if (this.getBulkRecord().get('operation') == 'assign') {
            var form = Ext.ComponentQuery.query('bulk-step3 issues-assign-form')[0].getForm();
            return !form || form.isValid();
        }
    },

    step1SelectedIssuesTxtHolderUptade: function (grid) {
        var step1SelectedIssuesTxt = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step1').down('[name=issues-qty-txt]');
        step1SelectedIssuesTxt.setText(
            grid.view.getSelectionModel().getSelection().length >= 1 ? (grid.view.getSelectionModel().getSelection().length +
                (grid.view.getSelectionModel().getSelection().length > 1 ? ' issues' : ' issue') + ' selected') : 'No issues selected');
    }
});