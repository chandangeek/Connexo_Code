Ext.define('Itk.view.overview.OverviewPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.overview-panel',
    requires: [
        'Itk.view.IssueFilter',
        'Itk.view.NoIssuesFoundPanel',
        'Isu.view.overview.Section'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'view-issues-filter',
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
                            flex: 1,
                            parentItemId: 'overview-of-issues',
                            route: 'workspace/issues'
                        },
                        items: [
                            {
                                title: Uni.I18n.translate('workspace.perStatus', 'ITK', 'Per status'),
                                itemId: 'status',
                                margin: '20 10 0 0'
                            },
                            {
                                title: Uni.I18n.translate('workspace.perWorkgroupAssignee', 'ITK', 'Per workgroup'),
                                itemId: 'workGroupAssignee',
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
                            flex: 1,
                            parentItemId: 'overview-of-issues',
                            route: 'workspace/issues'
                        },
                        items: [
                            {
                                title: Uni.I18n.translate('workspace.perUserAssignee', 'ITK', 'Per user'),
                                itemId: 'userAssignee',
                                margin: '20 10 0 0'
                            },
                            {
                                title: Uni.I18n.translate('workspace.perReason', 'ITK', 'Per reason'),
                                itemId: 'reason',
                                margin: '20 0 0 10'
                            }
                        ]
                    }
                ]
            }
        ]


        me.callParent(arguments);
    }
});
