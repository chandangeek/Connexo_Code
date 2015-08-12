Ext.define('Idc.controller.BulkChangeIssues', {
    extend: 'Isu.controller.BulkChangeIssues',

    refs: [
        {
            ref: 'page',
            selector: '#datacollection-bulk-browse'
        },
        {
            ref: 'bulkNavigation',
            selector: '#datacollection-bulk-browse bulk-navigation'
        }
    ],

    stores: [
        'Idc.store.IssuesBuffered',
        'Idc.store.BulkChangeIssues'
    ],

    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

    init: function () {
        this.control({
            '#datacollection-bulk-browse bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            '#datacollection-bulk-browse bulk-navigation': {
                movetostep: this.setActivePage
            },
            '#datacollection-bulk-browse bulk-wizard bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent,
                afterrender: this.getDefaultStep2Operation
            },
            '#datacollection-bulk-browse bulk-wizard bulk-step3 issues-close-form radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            '#datacollection-bulk-browse bulk-step4': {
                beforeactivate: this.beforeStep4
            },
            '#datacollection-bulk-browse bulk-wizard bulk-step3 issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            }
        });
    },

    showOverview: function () {
        this.callParent(['datacollection', 'Idc']);
    },

    onWizardCancelledEvent: function () {
        this.callParent(['datacollection']);
    },

    onBulkActionEvent: function () {
        this.callParent(['datacollection']);
    }
});