Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

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
        this.callParent(arguments);
    }
});