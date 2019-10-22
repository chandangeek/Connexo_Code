/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-issue-detail',
    requires: [
        'Isu.view.issues.DetailTop',
        'Iws.view.DetailForm',
        'Iws.view.WebServiceDetails',
        'Isu.view.issues.CommentsList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Iws.view.LogGrid'
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
                        itemId: 'webservice-issue-detail-previous-next-navigation-toolbar',
                        store: 'Isu.store.Issues',
                        router: me.router,
                        routerIdArgument: 'issueId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'issue-detail-top',
                itemId: 'webservice-issue-detail-top',
                router: me.router
            },
            {
                xtype: 'webservice-isssue-detail-form',
                itemId: 'issue-detail-form',
                router: me.router
            },
            {   xtype: 'panel',
                itemId: 'webservice-details-panel',
                ui: 'medium',
                title: Uni.I18n.translate('general.title.webServiceDetails', 'IWS', 'Web service request details'),
                items:[
                    {
                        xtype: 'webservice-details-form',
                        itemId: 'webservice-details-form',
                        router: me.router
                    }
                 ]
            },
            {
                xtype: 'webservice-issue-log-grid',
                itemId: 'webservice-issue-detail-log',
                title: Uni.I18n.translate('general.title.webServiceLogs', 'IWS', 'Web service occurence log'),
                router: me.router
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('general.title.webServiceContextual', 'IWS', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('general.title.webServiceTimeline', 'IWS', 'Timeline'),
                                itemId: 'tab-panel-issue-timeline',
                                items: [
                                    {
                                        xtype: 'issue-timeline',
                                        itemId: 'webservice-issue-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('general.title.webServiceComments', 'IWS', 'Comments'),
                                itemId: 'tab-panel-issue-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'webservice-issue-comments'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('general.title.webServiceProcesses', 'IWS', 'Processes'),
                                itemId: 'tab-panel-issue-processes',
                                privileges: Isu.privileges.Issue.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'webservice-issue-process'
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
