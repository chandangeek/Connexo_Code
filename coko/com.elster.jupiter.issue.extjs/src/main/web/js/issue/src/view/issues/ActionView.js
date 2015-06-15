Ext.define('Isu.view.issues.ActionView', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.ActionForm'
    ],
    alias: 'widget.issue-action-view',
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'issue-action-form',
                itemId: 'issue-action-view-form',
                title: ' ',
                ui: 'large',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});