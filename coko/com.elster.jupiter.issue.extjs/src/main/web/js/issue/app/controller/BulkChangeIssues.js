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

        var record = this.getBulkRecord();
        var requestData = this.getRequestData(record);

        var operation = record.get('operation');
        var requestUrl = '/api/isu/issue/' + operation;

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            jsonData: requestData,
            success: function (response, opts) {
                var obj = Ext.decode(response.responseText);
                console.log(obj);
            },
            failure: function (response, opts) {
                console.log('server-side failure with status code ' + response.status);
            }
        });
    },

    getRequestData: function (bulkStoreRecord) {
        var requestData = {issues: []};
        var operation = bulkStoreRecord.get('operation');
        var issues = bulkStoreRecord.get('issues');

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
                requestData.comment = bulkStoreRecord.get('comment');
                requestData.status = bulkStoreRecord.get('status');
                break;
        }

        return requestData;
    },

    onWizardCancelledEvent: function (wizard) {
        Ext.Msg.show({
            title: 'Operation',
            msg: 'Cancel has been pressed'
        });
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
            step3Panel.fireEvent('removechildborder', step3Panel);
        }
    },

    processNextOnStep3: function (wizard) {
        var record = this.getBulkRecord(),
            step4Panel = wizard.down('bulk-step4'),
            message = '',
            operation = record.get('operation'),
            status = record.get('status'),
            formPanel = Ext.ComponentQuery.query('bulk-step3 issues-assign-form')[0],
            activeRadio = formPanel.down('radiogroup').down('radio[checked=true]').inputValue,
            activeCombo = formPanel.down('combo[name=' + activeRadio + ']'),
            form = formPanel.getForm(),
            widget;

        if (form.isValid()) {
            record.set('assignee', {
                id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                type: activeCombo.name,
                title: activeCombo.rawValue
            });

        if (operation == 'assign') {
            message += '<h3>Assign '
        } else if (operation == 'close') {
            switch (status) {
                case 'CLOSED':
                    message += '<h3>Close ';
                    break;
                case 'REJECTED':
                    message += '<h3>Reject ';
                    break;
            }
        }

            message += record.get('issues').length;
            message += ' issues to ';
            message += record.get('assignee').title;
            message += '?</h3><br>';
            message += 'The selected issues will be assigned to ';
            message += record.get('assignee').title;

            widget = Ext.widget('container', {
                cls: 'isu-bulk-assign-confirmation-request-panel',
                html: message
            });

            if (widget) {
                step4Panel.removeAll(true);
                step4Panel.add(widget);
            }
        }
    },

    beforeStep4: function () {
        if (this.getBulkRecord().get('operation') == 'assign') {
            var form = Ext.ComponentQuery.query('bulk-step3 issues-assign-form')[0].getForm();
            return !form || form.isValid();
        }
    }
});