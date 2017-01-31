/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Idc.view.DetailsContainer',
        'Idc.view.LogGrid',
        'Isu.view.issues.CommentsList',
        'Idc.view.TimelineList',
        'Bpm.monitorissueprocesses.view.ProcessList',
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
                        store: 'Isu.store.Issues',
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
                xtype: 'container',
                itemId: 'data-collection-issue-detail-container',
                router: me.router
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.datacollection.context', 'IDC', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.datacollection.timeline', 'IDC', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'data-collection-issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.datacollection.comments', 'IDC', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'data-collection-issue-comments'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.datacollection.processes', 'IDC', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Isu.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'data-collection-issue-process'
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