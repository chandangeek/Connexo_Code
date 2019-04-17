/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Itk.view.DetailForm',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Itk.view.OccurrenceGrid'
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
                        itemId: 'task-issue-detail-previous-next-navigation-toolbar',
                        store: 'Isu.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'task-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'task-issue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            {   xtype: 'panel',
                itemId: 'occurrence-panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.task.logs', 'ITK', 'Task logs'),
                items:[
                    {
                        xtype: 'occurrence-details-grid',
                        itemId: 'task-issue-detail-container',
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.task.context', 'ITK', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.task.timeline', 'ITK', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'task-issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.task.comments', 'ITK', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'task-issue-comments'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.task.processes', 'ITK', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Isu.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'task-issue-process'
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