Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',

    doConversion: function (tokens) {
        if (tokens.length > 1) {
            if (tokens.length == 4 && !isNaN(tokens[3])) {
                this.showIssueDetail(parseInt(tokens[3]));
            } else if (tokens.length == 3) {
                switch (tokens[2]) {
                    case 'assignmentrules':
                        this.showAssigmentRules();
                        break;
                    case 'issues':
                        this.showIssues();
                        break;
                }
            } else if (tokens.length == 2 && tokens[1] == 'datacollection') {
                this.showDataCollection();
            }
        } else {
            this.showWorkspace();
        }
    },

    showWorkspace: function () {
        Isu.getApplication().getWorkspaceController().showOverview();
    },

    showDataCollection: function () {
        Isu.getApplication().getDataCollectionController().showOverview();
    },

    showIssues: function () {
        Isu.getApplication().getIssuesController().showOverview();
    },

    showIssueDetail: function (issueId) {
        Isu.getApplication().getIssueDetailController().showOverview(issueId);
    },

    showAssigmentRules: function () {
        Isu.getApplication().getIssueAssignmentRulesController().showOverview();
    }
});