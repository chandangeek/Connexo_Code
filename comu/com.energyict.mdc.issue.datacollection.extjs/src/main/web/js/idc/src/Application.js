Ext.define('Idc.Application', {
    name: 'Isu',
    appProperty: 'Current',

    extend: 'Ext.app.Application',

    controllers: [
        'Idc.controller.Main',
        'Idc.controller.history.Workspace',
        'Idc.controller.Issues',
        'Idc.controller.AssignIssues',
        'Idc.controller.CloseIssues',
        'Idc.controller.BulkChangeIssues',
        'Idc.controller.MessageWindow',
        'Idc.controller.IssueAssignmentRules',
        'Idc.controller.IssueCreationRules',
        'Idc.controller.IssueCreationRulesEdit',
        'Idc.controller.IssueCreationRulesActionsEdit',
        'Idc.controller.history.Workspace',
        'Idc.controller.IssueDetail',
        'Idc.controller.history.Administration',
        'Idc.controller.AdministrationDataCollection',
        'Idc.controller.NotifySend'
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