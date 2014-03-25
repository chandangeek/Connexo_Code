Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;

        crossroads.addRoute('administration',function(){
            Isu.getApplication().getAdministrationController().showOverview();
        });

        crossroads.addRoute('administration/datacollection',function(){
            Isu.getApplication().getAdministrationDataCollectionController().showOverview();
        });
        crossroads.addRoute('administration/datacollection/licensing',function(){
            Isu.getApplication().getLicensingController().showOverview();
        });
        crossroads.addRoute('administration/datacollection/licensing/addlicense',function(){
            Isu.getApplication().getAddLicenseController().showOverview();
        });
        crossroads.addRoute('administration/datacollection/issueassignmentrules',function(){
            Isu.getApplication().getIssueAssignmentRulesController().showOverview();
        });
        crossroads.addRoute('administration/datacollection/issuecreationrules',function(){
            Isu.getApplication().getIssueCreationRulesController().showOverview();
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