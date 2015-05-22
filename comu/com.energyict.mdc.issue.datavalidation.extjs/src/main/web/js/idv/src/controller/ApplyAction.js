Ext.define('Idv.controller.ApplyAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    models: [
        'Idv.model.Issue'
    ],

    refs: [
        {
            ref: 'page',
            selector: '#data-validation-issue-action-view'
        },
        {
            ref: 'form',
            selector: '#data-validation-issue-action-view #issue-action-view-form'
        }
    ],

    init: function () {
        this.control({
            '#data-validation-issue-action-view #issue-action-view-form #issue-action-apply': {
                click: this.applyAction
            }
        });
    },

    showOverview: function (issueId, actionId) {
        this.callParent(['Idv.model.Issue', issueId, actionId, 'data-validation-issue-action-view']);
    }
});