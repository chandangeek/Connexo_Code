Ext.define('Isu.controller.history.Workspace', {
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

    showWorkspace: function() {
        Isu.getApplication().getWorkspaceController().showOverview();
    },

    showDataCollection: function () {
        Isu.getApplication().getDataCollectionController().showOverview();
    },

    showIssues: function () {
        Isu.getApplication().getIssuesController().showOverview();
    },

    showAssigmentRules: function () {
        Isu.getApplication().getIssueAssignmentRulesController().showOverview();
    }
});