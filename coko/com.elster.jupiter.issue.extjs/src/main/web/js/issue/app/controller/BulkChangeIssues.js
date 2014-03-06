Ext.define('Mtr.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.Issues',
        'Mtr.store.IssuesGroups',
        'Mtr.store.BulkChangeIssues'
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
            'bulk-browse bulk-wizard bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            'bulk-browse bulk-step4': {
                beforeactivate: this.beforeStep4
            }
        });
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
        this.setBulkActionListActiveItem(wizard);
        this.getBulkActionsList().clearHyperLinkActions();

        var step5panel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step5'),
            record = this.getBulkRecord(),
            requestData = this.getRequestData(record),
            operation = record.get('operation'),
            requestUrl = '/api/isu/issue/' + operation;

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
                var obj = Ext.decode(response.responseText),
                    successCount = obj.success.length,
                    failedCount = obj.failure.length,
                    successMessage,
                    successWidget,
                    failedMessage,
                    failedWidget;

                switch (operation) {
                    case 'assign':
                        if (successCount > 0) {
                            successMessage = 'Successfully assigned ' + successCount + ' issue(s) to '
                                + '<b>' + record.get('assignee').title + '</b>';
                        }
                        if (failedCount > 0) {
                            failedMessage = 'Failed to assign ' + failedCount + 'issue(s)'
                        }
                        break;
                    case 'close':
                        if (successCount > 0) {
                            successMessage = 'Successfully closed ' + successCount + ' issue(s)';
                        }
                        if (failedCount > 0) {
                            failedMessage = 'Failed to close ' + failedCount + 'issue(s)'
                        }
                        break;
                }

                step5panel.removeAll(true);

                if (successCount > 0) {
                    successWidget = Ext.widget('container', {
                        cls: 'isu-bulk-close-success-panel',
                        html: successMessage
                    });
                    step5panel.add(successWidget);
                }

                if (failedCount > 0) {
                    failedWidget = Ext.widget('container', {
                        cls: 'isu-bulk-close-failed-panel',
                        html: successMessage
                    });
                    step5panel.add(failedWidget);
                }
            },
            failure: function (response) {
                step5panel.removeAll(true);
                console.log('server-side failure with status code ' + response.status);
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
        var bulkStore = Ext.getStore('Mtr.store.BulkChangeIssues');
        var bulkRecord = bulkStore.getAt(0);
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
        record.commit();
    },

    getDefaultCloseStatus: function () {
        var formPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step3').down('issues-close'),
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
                view = 'issues-close';
                break;
        }

        widget = Ext.widget(view, {bulk: true});

        if (widget) {
            step3Panel.removeAll(true);
            step3Panel.add(widget);
            if (view === 'issues-assign-form') {
                step3Panel.fireEvent('removechildborder', step3Panel);
            }
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

    afterStep5: function (result) {

    }

});