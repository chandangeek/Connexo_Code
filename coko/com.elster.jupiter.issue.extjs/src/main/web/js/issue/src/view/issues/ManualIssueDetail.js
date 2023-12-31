/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.ManualIssueDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.manual-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Isu.view.issues.ManualIssueDetailForm',
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
                        itemId: 'data-validation-issue-detail-previous-next-navigation-toolbar',
                        store: 'Isu.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'manual-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'manual-issue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            {
                xtype: 'issue-comments',
                itemId: 'manual-issue-comments'
            }
        ];

        me.callParent(arguments);
    }
});