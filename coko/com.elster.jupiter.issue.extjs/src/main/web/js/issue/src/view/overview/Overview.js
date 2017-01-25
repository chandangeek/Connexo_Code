Ext.define('Isu.view.overview.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.overview-of-issues',
    requires: [
        'Isu.view.issues.IssueFilter',
        'Isu.view.overview.Section',
        'Isu.view.issues.NoIssuesFoundPanel'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.issuesOverview', 'ISU', 'Issues overview'),
                ui: 'large',
                itemId: 'overview-of-issues-main-panel',
                items: [
                    {
                        xtype: 'isu-view-issues-issuefilter',
                        itemId: 'overview-of-issues-issuefilter',
                        store: new Ext.data.ArrayStore(),
                        isOverviewFilter: true
                    },
                    {
                        xtype: 'no-issues-found-panel',
                        itemId: 'overview-no-issues-found-panel',
                        hidden: true
                    },
                    {
                        itemId: 'sections-panel',
                        hidden: true,
                        items: [
                            {
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    xtype: 'overview-of-issues-section',
                                    ui: 'tile',
                                    flex: 1
                                },
                                items: [
                                    {
                                        title: Uni.I18n.translate('workspace.perType', 'ISU', 'Per type'),
                                        itemId: 'issueType',
                                        margin: '10 10 0 0'
                                    },
                                    {
                                        title: Uni.I18n.translate('workspace.perStatus', 'ISU', 'Per status'),
                                        itemId: 'status',
                                        margin: '10 0 0 10'
                                    }
                                ]
                            },
                            {
                                layout: {
                                    type: 'hbox'
                                },
                                defaults: {
                                    xtype: 'overview-of-issues-section',
                                    ui: 'tile',
                                    flex: 1
                                },
                                items: [
                                    {
                                        title: Uni.I18n.translate('workspace.perUserAssignee', 'ISU', 'Per user'),
                                        itemId: 'userAssignee',
                                        margin: '20 10 0 0'
                                    },
                                    {
                                        title: Uni.I18n.translate('workspace.perReason', 'ISU', 'Per reason'),
                                        itemId: 'reason',
                                        margin: '20 0 0 10'
                                    }
                                ]
                            },
                            {
                                layout: {
                                    type: 'hbox'
                                },
                                defaults: {
                                    xtype: 'overview-of-issues-section',
                                    ui: 'tile',
                                    flex: 1
                                },
                                items: [
                                    {
                                        title: Uni.I18n.translate('workspace.perWorkgroupAssignee', 'ISU', 'Per workgroup'),
                                        itemId: 'workGroupAssignee',
                                        margin: '20 10 0 0'
                                    },
                                    {
                                        xtype: 'container'
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