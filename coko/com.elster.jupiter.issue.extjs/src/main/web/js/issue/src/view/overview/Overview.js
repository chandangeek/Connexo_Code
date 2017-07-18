Ext.define('Isu.view.overview.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.overview-of-issues',
    requires: [
        'Isu.view.overview.OverviewPanel',
        'Isu.view.overview.HistoryPanel'
    ],
    activeTab: 0,
    initComponent: function () {
        var me = this;

        me.content = {
            title: Uni.I18n.translate('workspace.issuesOverview', 'ISU', 'Issues overview'),
            ui: 'large',
            items: [
                {
                    xtype: 'tabpanel',
                    itemId: 'tab-panel-overview',
                    activeTab: me.activeTab,
                    items: [
                        {
                            ui: 'medium',
                            title: Uni.I18n.translate('overview.overview', 'ISU', 'Overview'),
                            itemId: 'tab-overview-panel',
                            items: [
                                {
                                    xtype: 'overview-issues-panel',
                                    itemId: 'pnl-overview'
                                }
                            ]
                        },
                        {
                            ui: 'medium',
                            title: Uni.I18n.translate('overview.history', 'ISU', 'History'),
                            itemId: 'tab-history-panel',
                            items: [
                                {
                                    xtype: 'history-issues-panel',
                                    itemId: 'pnl-history'
                                }
                            ]
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});