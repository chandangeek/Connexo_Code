Ext.define('Dal.controller.SetPriority', {
    extend: 'Isu.controller.SetPriority',
    issueModel: 'Dal.model.Alarm',

    init: function () {
        var me = this;

        me.control({
            'alarm-set-priority button[action=savePriority]': {
                click: this.savePriority
            }
        });
    }

});
