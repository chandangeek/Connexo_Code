/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecall-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Isc.view.DetailForm',
        'Isc.view.ServiceCallDetails',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Isc.view.LogGrid'
    ],
    router: null,
    store: null,
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
                        itemId: 'servicecall-issue-detail-previous-next-navigation-toolbar',
                        store: 'Isu.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'servicecall-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'servicecall-isssue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            {   xtype: 'panel',
                itemId: 'servicecall-details-panel',
                ui: 'medium',
                title: Uni.I18n.translate('general.servicecall.details.title', 'ISC', 'Service call details'),
                items:[
                    {
                        xtype: 'servicecall-details-form',
                        itemId: 'servicecall-details-form',
                        router: me.router
                    }
                 ]
            },
            {   xtype: 'panel',
                itemId: 'servicecall-issue-log-grid',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.servicecall.logs', 'ISC', 'Most recent service call log'),
                items:[
                    {
                        xtype: 'servicecall-issue-log-grid',
                        itemId: 'servicecall-issue-detail-log',
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('issue.workspace.servicecall.context', 'ISC', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.servicecall.timeline', 'ISC', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'servicecall-issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.servicecall.comments', 'ISC', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'servicecall-issue-comments'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('issue.workspace.servicecall.processes', 'ISC', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Isu.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'servicecall-issue-process'
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
