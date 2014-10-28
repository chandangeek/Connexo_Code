Ext.define('Idc.controller.ApplyAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    models: [
        'Idc.model.Issue'
    ],

    refs: [
        {
            ref: 'page',
            selector: '#data-collection-issue-action-view'
        },
        {
            ref: 'form',
            selector: '#data-collection-issue-action-view #issue-action-view-form'
        }
    ],

    init: function () {
        this.control({
            '#data-collection-issue-action-view #issue-action-view-form #issue-action-apply': {
                click: this.applyAction
            }
        });
    },

    showOverview: function (issueId, actionId) {
        this.callParent(['Idc.model.Issue', issueId, actionId, 'data-collection-issue-action-view']);
    }
});