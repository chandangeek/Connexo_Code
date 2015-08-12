Ext.define('Idv.controller.BulkChangeIssues', {
    extend: 'Isu.controller.BulkChangeIssues',

    refs: [
        {
            ref: 'page',
            selector: '#datavalidation-bulk-browse'
        },
        {
            ref: 'bulkNavigation',
            selector: '#datavalidation-bulk-browse bulk-navigation'
        }
    ],

    stores: [
        'Idv.store.IssuesBuffered',
        'Idv.store.BulkChangeIssues'
    ],

    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

    init: function () {
        this.control({
            '#datavalidation-bulk-browse bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            '#datavalidation-bulk-browse bulk-navigation': {
                movetostep: this.setActivePage
            },
            '#datavalidation-bulk-browse bulk-wizard bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent,
                afterrender: this.getDefaultStep2Operation
            },
            '#datavalidation-bulk-browse bulk-wizard bulk-step3 issues-close-form radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            '#datavalidation-bulk-browse bulk-step4': {
                beforeactivate: this.beforeStep4
            },
            '#datavalidation-bulk-browse bulk-wizard bulk-step3 issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            }
        });
    },

    showOverview: function () {
        this.callParent(['datavalidation', 'Idv']);
    },

    onWizardCancelledEvent: function () {
        this.callParent(['datavalidation']);
    },

    onBulkActionEvent: function () {
        this.callParent(['datavalidation']);
    }
});