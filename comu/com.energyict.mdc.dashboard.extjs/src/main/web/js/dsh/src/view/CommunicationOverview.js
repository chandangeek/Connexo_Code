/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.CommunicationOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown'
    ],
    alias: 'widget.communication-overview',
    itemId: 'communication-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '0 20px'
    },
    defaults: {
        style: {
            marginBottom: '20px',
            padding: 0
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                router: me.router,
                style: 'none'
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    flex: 1
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 2.05,
                        parent: 'communications',
                        router: me.router
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router,
                        flex: 1
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        maxHeight:256,
                        flex: 1,
                        overflowY:'auto',
                        style: {
                            marginRight: '0',
                            padding: '20px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.viewAll', 'DSH', 'View all communications'),
                                href: me.router.getRoute('workspace/communications/details').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: me.router.getRoute('workspace/connections').title,
                                href: me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.myIssues', 'DSH', 'My open issues'),
                                href: typeof me.router.getRoute('workspace/issues') !== 'undefined'
                                    ? me.router.getRoute('workspace/issues').buildUrl(null, me.router.queryParams) + '?myopenissues=true&issueType=datacollection' : null
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'read-outs-over-time',
                wTitle: Uni.I18n.translate('communications.widget.readOutsOverTime.title', 'DSH', 'Communications over time'),
                yLabel: Uni.I18n.translate('communications.widget.readOutsOverTime.yLabel', 'DSH', 'Number of communications'),
                emptyMsg: Uni.I18n.translate('communications.widget.readOutsOverTime.emptyMsg', 'DSH', 'Communications over time are not measured for the selected group.'),
                router: me.router,
                parent: 'communications'
            },
            {
                xtype: 'overview',
                category: 'Communication',
                parent: 'communications',
                router: me.router
            },
            {
                xtype: 'breakdown',
                parent: 'communications',
                router: me.router
            },
            {
                xtype: 'heat-map',
                itemId: 'heatmap',
                store: 'Dsh.store.CommunicationResultsStore',
                router: me.router,
                parent: 'communications'
            }
        ];
        this.callParent(arguments);
    }
});