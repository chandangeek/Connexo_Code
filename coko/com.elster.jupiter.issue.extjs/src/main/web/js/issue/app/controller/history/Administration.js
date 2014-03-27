Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'issue-administration',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;

        crossroads.addRoute('issue-administration',function(){
            me.getController('Isu.controller.Administration').showOverview();
        });

        crossroads.addRoute('issue-administration/datacollection',function(){
            me.getController('Isu.controller.AdministrationDataCollection').showOverview();
        });
        crossroads.addRoute('issue-administration/datacollection/licensing',function(){
            me.getController('Isu.controller.Licensing').showOverview();
        });
        crossroads.addRoute('issue-administration/datacollection/licensing/addlicense',function(){
            me.getController('Isu.controller.AddLicense').showOverview();
        });
        crossroads.addRoute('issue-administration/datacollection/issueassignmentrules',function(){
            me.getController('Isu.controller.IssueAssignmentRules').showOverview();
        });
        crossroads.addRoute('issue-administration/datacollection/issuecreationrules',function(){
            me.getController('Isu.controller.IssueCreationRules').showOverview();
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