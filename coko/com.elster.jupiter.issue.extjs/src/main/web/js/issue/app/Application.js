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
        'history.Workspace'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    init: function () {
        // TODO App specific loading.
        this.callParent(arguments);
    },

    launch: function () {
        this.fireEvent('changeapptitleevent', 'Jupiter issue application');
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});