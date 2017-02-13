Ext.define('Dal.view.overview.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.overview-of-alarms',
    requires: [
        'Dal.view.overview.OverviewPanel',
        'Dal.view.overview.HistoryPanel'
    ],
    activeTab: 0,
    initComponent: function () {
        var me = this;

        me.content = {
            title: Uni.I18n.translate('workspace.alarmsOverview', 'DAL', 'Alarms overview'),
            ui: 'large',
            items: [
                {
                    xtype: 'tabpanel',
                    itemId: 'tab-panel-overview',
                    activeTab: me.activeTab,
                    items: [
                        {
                            ui: 'medium',
                            title: Uni.I18n.translate('overview.overview', 'DAL', 'Overview'),
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
                            title: Uni.I18n.translate('overview.history', 'DAL', 'History'),
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