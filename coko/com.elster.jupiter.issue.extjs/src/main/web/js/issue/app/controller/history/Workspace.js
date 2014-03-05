Ext.define('Mtr.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',

    doConversion: function (tokens) {
        if (tokens.length > 1) {
            if (tokens.length == 3 && tokens[2] == 'assignmentrules') {
                this.showAssigmentRules();
            } else if (tokens.length == 3 && tokens[2] == 'issues') {
                this.showIssues();
            } else if (tokens.length == 2 && tokens[1] == 'datacollection') {
                this.showDataCollection();
            }
        } else {
            this.showWorkspace();
        }
    },

    showWorkspace: function () {
        Mtr.getApplication().getWorkspaceController().showOverview();
    },

    showDataCollection: function () {
        Mtr.getApplication().getDataCollectionController().showOverview();
    },

    showIssues: function () {
        Mtr.getApplication().getIssuesController().showOverview();
    },

    showAssigmentRules: function () {
        Mtr.getApplication().getIssueAssignmentRulesController().showOverview();
    }
});