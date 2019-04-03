Ext.define('Itk.view.overview.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.overview-of-issues',
    requires: [
        'Itk.view.overview.OverviewPanel',
        'Itk.view.overview.HistoryPanel'
    ],
    activeTab: 0,
    initComponent: function () {
        var me = this;

        me.content = {
            title: Uni.I18n.translate('workspace.issuesOverview', 'ITK', 'Issues overview'),
            ui: 'large',
            items: [
                {
                    xtype: 'tabpanel',
                    itemId: 'tab-panel-overview',
                    activeTab: me.activeTab,
                    items: [
                        {
                            ui: 'medium',
                            title: Uni.I18n.translate('overview.overview', 'ITK', 'Overview'),
                            itemId: 'tab-overview-panel',
                            items: [
                                {
                                    xtype: 'overview-panel',
                                    itemId: 'pnl-overview'
                                }
                            ]
                        },
                        {
                            ui: 'medium',
                            title: Uni.I18n.translate('overview.history', 'ITK', 'History'),
                            itemId: 'tab-history-panel',
                            items: [
                                {
                                    xtype: 'history-panel',
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