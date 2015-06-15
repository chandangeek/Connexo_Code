Ext.define('Isu.view.creationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-creation-rules-edit-action',
    requires: [
        'Isu.view.creationrules.EditActionForm'
    ],
    isEdit: false,
    returnLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'issues-creation-rules-edit-action-form',
                itemId: 'issues-creation-rules-edit-action-form',
                title: me.router.getRoute().getTitle(),
                ui: 'large',
                isEdit: me.isEdit,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});