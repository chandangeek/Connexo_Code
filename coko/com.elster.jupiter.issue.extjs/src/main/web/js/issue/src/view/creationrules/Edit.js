Ext.define('Isu.view.creationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.creationrules.EditForm'
    ],
    alias: 'widget.issues-creation-rules-edit',
    router: null,
    isEdit: false,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'issues-creation-rule-edit-form',
                xtype: 'issues-creation-rule-edit-form',
                ui: 'large',
                router: me.router,
                isEdit: me.isEdit
            }
        ];

        me.callParent(arguments);
    }
});