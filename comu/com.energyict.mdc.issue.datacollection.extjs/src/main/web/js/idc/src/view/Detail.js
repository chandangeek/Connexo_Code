Ext.define('Idc.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Idc.view.DetailForm',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation'
    ],
    router: null,
    issuesListLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        itemId: 'issue-detail-top-title',
                        ui: 'large',
                        flex: 1
                    },
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        margin: '10 0 0 0',
                        itemId: 'data-collection-issue-detail-previous-next-navigation-toolbar',
                        store: 'Idc.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'data-collection-issue-detail-top',
                router: me.router
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