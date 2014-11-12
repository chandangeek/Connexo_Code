Ext.define('Isu.controller.AssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'Isu.view.assignmentrules.Overview'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-assignment-rules-overview issues-assignment-rules-list gridview': {
                refresh: this.setAssigneeTypeIconTooltip
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.getStore('Isu.store.AssignmentRules').load();
    }
});
