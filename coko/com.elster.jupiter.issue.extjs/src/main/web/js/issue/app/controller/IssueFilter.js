Ext.define('Isu.controller.IssueFilter', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.Assignee'
    ],

    views: [
        'workspace.issues.SideFilter'
    ],

    refs: [

    ],

    init: function () {
        this.control({
            'issues-side-filter button[action="filter"]': {
                click: this.filter
            }
        });
    },

    filter: function() {
        console.log('filter');
    }
});