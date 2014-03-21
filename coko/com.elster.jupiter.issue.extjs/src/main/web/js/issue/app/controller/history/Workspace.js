Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;

        crossroads.addRoute('workspace',function(){
            Isu.getApplication().getWorkspaceController().showOverview();
        });

        crossroads.addRoute('workspace/datacollection',function(){
            Isu.getApplication().getDataCollectionController().showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues',function(){
            Isu.getApplication().getIssuesController().showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issuesbulkaction',function(){
            Isu.getApplication().getBulkChangeIssuesController().showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}',function(id){
            Isu.getApplication().getIssueDetailController().showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/assign',function(id){
            Isu.getApplication().getAssignIssuesController().showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/close',function(id){
            Isu.getApplication().getCloseIssuesController().showOverview(id);
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens,token) {
        //now has tokens and token (which is you complete path)

        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if (this.currentPath !== null) {
            this.previousPath = this.currentPath;
        }
        this.currentPath = token;
        crossroads.parse(token);
    }

//    doConversion: function (tokens) {
//        if (tokens.length > 1) {
//            if (tokens.length == 4 && !isNaN(tokens[3])) {
//                this.showIssueDetail(parseInt(tokens[3]));
//            } else if (tokens.length == 3) {
//                switch (tokens[2]) {
//                    case 'assignmentrules':
//                        this.showAssigmentRules();
//                        break;
//                    case 'issues':
//                        this.showIssues();
//                        break;
//                }
//            } else if (tokens.length == 2 && tokens[1] == 'datacollection') {
//                this.showDataCollection();
//            }
//        } else {
//            this.showWorkspace();
//        }
//    }
});
