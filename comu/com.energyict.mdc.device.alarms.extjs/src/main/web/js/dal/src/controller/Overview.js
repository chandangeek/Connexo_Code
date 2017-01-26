Ext.define('Dal.controller.Overview', {
    extend: 'Isu.controller.Overview',

    models: [
        'Dal.model.Group'
    ],

    stores: [
        'Dal.store.AlarmStatuses',
        'Dal.store.DueDate'
    ],

    views: [
        'Dal.view.Overview'
    ],

    sections: ['status', 'userAssignee', 'reason', 'workGroupAssignee'],

    widgetType: 'overview-of-alarms',
    model: 'Dal.model.Group',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'overview',
                selector: 'overview-of-alarms'
            },
            {
                ref: 'filterToolbar',
                selector: 'overview-of-alarms view-alarms-filter'
            },
            {
                ref: 'noPanelFound',
                selector: 'overview-of-alarms #overview-no-alarms-found-panel'
            }
        ];

        me.callParent(arguments);
    },

    init: function () {
        this.control({
            'overview-of-alarms button[action=applyAll]': {
                click: this.updateSections
            },
            'overview-of-alarms button[action=clearAll]': {
                click: this.clearAllFilters
            }
        });
    }
});
