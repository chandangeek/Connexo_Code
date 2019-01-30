/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Idl.view.DetailForm',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Idl.view.NonEstimatedDataGrid'
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
                itemId: 'data-validation-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'data-validation-issue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            {
                xtype: 'container',
                itemId: 'no-estimated-data-panel',
                title: Uni.I18n.translate('issues.NonEstimatedDataGrid.title', 'IDL', 'Non estimated data'),
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