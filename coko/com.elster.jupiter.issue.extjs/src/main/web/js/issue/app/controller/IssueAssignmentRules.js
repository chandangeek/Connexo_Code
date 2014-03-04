Ext.define('Mtr.controller.IssueAssignmentRules', {
        extend: 'Ext.app.Controller',

        stores: [
            'Mtr.store.AssignmentRules'
        ],
        views: [
            'workspace.issues.AssignmentRulesList'
        ],

        refs: [],

        init: function () {
            this.control({

            });

        }
    }
);
