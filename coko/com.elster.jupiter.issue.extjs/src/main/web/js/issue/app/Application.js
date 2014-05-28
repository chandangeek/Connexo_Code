Ext.define('Isu.Application', {
    name: 'Isu',
    appProperty: 'Current',

    extend: 'Ext.app.Application',

    controllers: [
        'Isu.controller.Main',
        'Isu.controller.history.Workspace',
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.IssueCreationRulesEdit',
        'Isu.controller.history.Workspace',
        'Isu.controller.IssueDetail',
        'Isu.controller.history.Administration',
        'Isu.controller.AdministrationDataCollection',
        'Isu.controller.Notify'
    ],

    init: function () {
        // TODO App specific loading.
        this.callParent(arguments);
    },

    launch: function () {
        //this.fireEvent('changeapptitleevent', 'Jupiter issue application');
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});