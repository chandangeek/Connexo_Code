Ext.define('Isu.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'administration.datacollection.issueassignmentrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issueassignmentrules.ActionMenu'
    ],

    mixins: {
            isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-assignment-rules-overview issues-assignment-rules-list gridview': {
                refresh: this.onGridRefresh
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
    }
});
