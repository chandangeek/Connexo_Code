Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    doConversion: function (tokens) {
        if (tokens.length > 1) {
            if (tokens.length == 3) {
                switch (tokens[2]) {
                    case 'issueassignmentrules':
                        this.showAssigmentRules();
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
        Isu.getApplication().getAdministrationController().showOverview();
    },

    showDataCollection: function () {
        Isu.getApplication().getAdministrationDataCollectionController().showOverview();
    },

    showAssigmentRules: function () {
        Isu.getApplication().getIssueAssignmentRulesController().showOverview();
    }
});