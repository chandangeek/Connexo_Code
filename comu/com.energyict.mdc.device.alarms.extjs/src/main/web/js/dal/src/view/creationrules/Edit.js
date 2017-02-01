Ext.define('Dal.view.creationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Dal.view.creationrules.EditForm'
    ],
    alias: 'widget.alarms-creation-rules-edit',
    router: null,
    isEdit: false,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'alarms-creation-rule-edit-form',
                xtype: 'alarms-creation-rule-edit-form',
                ui: 'large',
                router: me.router,
                isEdit: me.isEdit
            }
        ];

        me.callParent(arguments);
    }
});