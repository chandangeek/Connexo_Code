Ext.define('Isu.Application', {
    name: 'Isu',

    extend: 'Ext.app.Application',

    requires: [

    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Main',
        'Workspace',
        'DataCollection',
        'Issues',
        'AssignIssues',
        'CloseIssues',
        'BulkChangeIssues',
        'MessageWindow',
        'IssueAssignmentRules',
        'IssueAutoCreationRules',
        'history.Workspace',
        'IssueFilter',
        'IssueDetail',
        'history.Administration',
        'Administration',
        'AdministrationDataCollection'
    ],

    stores: [
        'Isu.store.Issues'
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