/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-lifecycle-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Idl.view.DetailForm',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Idl.view.TransitionGrid'
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
                xtype: 'device-lifecycle-issue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            // {
            //     xtype: 'container',
            //     itemId: 'no-estimated-data-panel',
            //     title: Uni.I18n.translate('issues.NonEstimatedDataGrid.title', 'IDL', 'Non estimated data'),
            //     router: me.router
            // },
            // {
            //     xtype: 'issue-comments',
            //     itemId: 'data-validation-issue-comments'
            // },
            {   xtype: 'panel',
                itemId: 'transition-panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.devicelifecycle.transitions', 'IDL', 'Transitions'),
                items:[
                    {
                        xtype: 'transition-details-grid',
                        itemId: 'device-lifecycle-issue-detail-container',
                        router: me.router
                    }
                 ]
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.devicelifecycle.context', 'IDL', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.devicelifecycle.timeline', 'IDL', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'device-lifecycle-issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.devicelifecycle.comments', 'IDL', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'device-lifecycle-issue-comments'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.devicelifecycle.processes', 'IDL', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Isu.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'device-lifecycle-issue-process'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});