Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;

        crossroads.addRoute('administration/issueassignmentrules', function () {
            me.getController('Isu.controller.IssueAssignmentRules').showOverview();
        });
        crossroads.addRoute('administration/issuecreationrules', function () {
            me.getController('Isu.controller.IssueCreationRules').showOverview();
        });
        crossroads.addRoute('administration/issuecreationrules/create', function (id) {
            me.getController('Isu.controller.IssueCreationRulesEdit').showOverview(id, 'create');
        });
        crossroads.addRoute('administration/issuecreationrules/{id}/edit', function (id) {
            me.getController('Isu.controller.IssueCreationRulesEdit').showOverview(id, 'edit');
        });
        crossroads.addRoute('issue-administration/communicationtasks',function(){
            me.getController('Isu.controller.CommunicationTasksView').showOverview();
        });
        crossroads.addRoute('issue-administration/communicationtasks/create',function(){
            me.getController('Isu.controller.CommunicationTasksEdit').showOverview();
        });
        crossroads.addRoute('issue-administration/communicationtasks/{id}',function(id){
            me.getController('Isu.controller.CommunicationTasksEdit').showOverview(id);
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens, token) {
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