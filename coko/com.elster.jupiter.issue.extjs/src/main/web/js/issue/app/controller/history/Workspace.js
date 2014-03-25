Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,
    requires: [
        'Isu.controller.Workspace'
    ],

    init: function () {
        var me = this;

        console.log('Isu.controller.history.Workspace - init');

        crossroads.addRoute('workspace',function(){
            me.getController('Isu.controller.Workspace').showOverview();
        });

        crossroads.addRoute('workspace/datacollection',function(){
            me.getController('Isu.controller.DataCollection').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues',function(){
            me.getController('Isu.controller.Issues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issuesbulkaction',function(){
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}',function(id){
            me.getController('Isu.controller.IssueDetail').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/addcomment',function(id){
            me.getController('Isu.controller.IssueDetail').showOverview(id, true);
        });
        crossroads.addRoute('workspace/datacollection/bulkaction',function(){
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/assign',function(id){
            me.getController('Isu.controller.AssignIssues').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/close',function(id){
            me.getController('Isu.controller.CloseIssues').showOverview(id);
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
});
