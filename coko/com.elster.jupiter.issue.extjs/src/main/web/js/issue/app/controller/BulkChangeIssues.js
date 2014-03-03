Ext.define('Mtr.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.Issues',
        'Mtr.store.IssuesGroups'
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
        var widget = Ext.widget('bulkMain');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function(wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setHyperLinkAction(index, false);
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function(wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setHyperLinkAction(index - 1, true);
        this.setBulkActionListActiveItem(wizard);

        var functionName =  'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function(wizard) {
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardFinishedEvent: function(wizard) {
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
            success: function(response, opts) {
                var obj = Ext.decode(response.responseText);
                console.log(obj);
            },
            failure: function(response, opts) {
                console.log('server-side failure with status code ' + response.status);
            }
        });
    },

    getRequestData: function(bulkStoreRecord) {
        var requestData = {
                issues: []
            },
            issues,
            operation = bulkStoreRecord.get('operation');

        issues = bulkStoreRecord.get('issues');
        Ext.iterate(issues, function(issue, index, issuesItSelf) {
            requestData.issues.push(
                {
                    id: issue.id,
                    version: issue.version
                }
            );
        });

        switch (operation) {
            case 'assign':
                requestData.assignee = {
                    id: bulkStoreRecord.get('assigneeId'),
                    type: bulkStoreRecord.get('assigneeType')
                };
                break
            case 'close':
                requestData.comment = bulkStoreRecord.get('comment');
                requestData.status = bulkStoreRecord.get('status');
                break
        }

        return requestData;
    },

    onWizardCancelledEvent: function(wizard) {
        Ext.Msg.show({
            title: 'Operation',
            msg: 'Cancel has been pressed'
        });
    },

    setBulkActionListActiveItem: function(wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkActionsList().setActiveAction(index);
    },

    processStep: function(func, wizard) {
        if(func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function() {
        var bulkStore = Ext.getStore('Isu.store.bulk.Bulk');
        var bulkRecord = bulkStore.getAt(0);
        if(!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }
        return bulkStore.getAt(0);
    },

    onStep2RadiogroupChangeEvent: function(radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('operation', newValue.operation);
        record.commit();
    },

    processNextOnStep1: function(wizard) {
        var record = this.getBulkRecord();

        record.set('issues', [
            {
                id: '1',
                version: '1'
            },
            {
                id: '2',
                version: '1'
            }
        ]);

        record.commit();
    },

    processNextOnStep2: function(wizard) {
        var record = this.getBulkRecord();
        var step3Panel = wizard.down('bulk-step3');

        var operation = record.get('operation');
        var widget = Ext.widget('bulk-' + operation + '-issue');
        if(widget) {
            step3Panel.removeAll(true);
            step3Panel.add(widget);
        }
    },

    processNextOnStep3: function(wizard) {
        var record = this.getBulkRecord(),
            step4Panel = wizard.down('bulk-step4'),
            message = '';

        var operation = record.get('operation');

        switch (operation) {
            case 'assign':
                message += 'Assign ';
                break
            case 'close':
                message += 'Close ';
                break
        }

        message += 'issues?';
        var widget = Ext.widget('panel', {
            html: message
        });
        if(widget) {
            step4Panel.removeAll(true);
            step4Panel.add(widget);
        }
    }
});