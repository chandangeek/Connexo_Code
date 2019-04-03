/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-detail',
    requires: [
        'Itk.view.DetailTop',
        'Itk.view.Preview',
        'Itk.view.IssueFilter',
        'Itk.view.EventsIssueDetailsForm',
        'Isu.view.issues.CommentsList',
        'Itk.view.TimelineList',
        'Bpm.monitorissueprocesses.view.ProcessList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Itk.store.Issues'
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
                        itemId: 'issue-detail-previous-next-navigation-toolbar',
                        store: 'Itk.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'issue-detail-top',
                router: me.router
            },
            {
                xtype: 'events-issue-details-form',
                itemId: 'events-issue-details-form',
                router: me.router,
                store: me.store
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.context', 'ITK', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.timeline', 'ITK', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.comments', 'ITK', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'issue-comments',
                                        noCommentText: Uni.I18n.translate('general.NoIssueCommentsCreatedYet', 'ITK', 'No comments created yet on this issue'),
                                        addCommentPrivileges: Itk.privileges.Issue.comment
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.processes', 'ITK', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Itk.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'issue-process',
                                        noProcessText: Uni.I18n.translate('processes.issue.noProcessesStarted', 'ITK', 'No process started yet on this issue'),
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