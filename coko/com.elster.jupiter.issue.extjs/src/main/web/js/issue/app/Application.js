Ext.define('Mtr.Application', {
    name: 'Mtr',

    extend: 'Ext.app.Application',

    requires: [
        'Mtr.model.types.Quantity'
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Main',
        'Error',
        'Party',
        'Dashboard',
        'Playground',
        'UsagePoint',
        'Person',
        'Group',
        'User',
        'Workspace',
        'DataCollection',
        'Issues',
        'AssignIssues',
        'CloseIssues',
        'BulkChangeIssues',
        'MessageWindow',
        'IssueAssignmentRules',

        'history.Dashboard',
        'history.Group',
        'history.Party',
        'history.Person',
        'history.Playground',
        'history.UsagePoint',
        'history.User',
        'history.Workspace'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    init: function () {
        // TODO App specific loading.
        this.callParent(arguments);
        this.getApplication().fireEvent('changeapptitleevent', 'This is just a test');
    },

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});