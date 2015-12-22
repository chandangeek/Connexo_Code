Ext.define('Idv.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Idv.view.DetailForm',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Idv.view.NonEstimatedDataGrid'
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
                        itemId: 'data-validation-issue-detail-previous-next-navigation-toolbar',
                        store: 'Idv.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'data-validation-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'data-validation-issue-detail-form',
                itemId: 'data-validation-issue-detail-form',
                router: me.router
            },
            {
                xtype: 'container',
                margin: '0 0 0 16',
                itemId: 'no-estimated-data-panel',
                title: Uni.I18n.translate('issues.NonEstimatedDataGrid.title', 'IDV', 'Non estimated data'),
                router: me.router
            },
            {
                xtype: 'issue-comments',
                itemId: 'data-validation-issue-comments'
            }
        ];

        me.callParent(arguments);
    }
});