Ext.define('Idc.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-issue-detail',
    requires: [
        'Isu.view.issues.DetailNavigation',
        'Isu.view.issues.DetailTop',
        'Idc.view.DetailForm',
        'Isu.view.issues.CommentsList'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'issue-detail-navigation',
                itemId: 'data-collection-issue-detail-navigation'
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'data-collection-issue-detail-top'
            },
            {
                xtype: 'data-collection-issue-detail-form',
                itemId: 'data-collection-issue-detail-form',
                router: me.router
            },
            {
                xtype: 'issue-comments',
                itemId: 'data-collection-issue-comments'
            }
        ];

        me.callParent(arguments);
    }
});