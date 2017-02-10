Ext.define('Dal.view.overview.HistoryPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.history-panel',
    requires: [
        'Dal.view.overview.HistoryGraph',
        'Dal.view.overview.HistoryFilter',
        'Dal.view.NoAlarmsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'view-history-filter',
                itemId: 'view-history-filter'
            },
            {
                xtype: 'no-alarms-found-panel',
                itemId: 'overview-no-history-alarms-found-panel',
                hidden: true
            },
            {
                xtype: 'panel',
                itemId: 'overview-graphs-panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                //        hidden: true,
                items: [
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.alarmsPerReason', 'DAL', 'Alarms per reason'),
                        itemId: 'crt-alarms-per-reason',
                        margin: '20 10 0 0',
                        showLegend: false,
                        tooltipAlarmMsg: Uni.I18n.translate('alarms.alarmsPerReason', 'DAL', '{0}: {1} alarms'),
                        url: '/api/dal/history',
                        field: 'reasonsPerDay'
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.alarmsOpenClosed', 'DAL', 'Alarms open vs closed'),
                        itemId: 'crt-alarms-open-closed',
                        margin: '20 10 0 0',
                        defaultFields: ['open', 'closed'],
                        defaultFields2: [{name: 'open', type: 'int'}, {name: 'closed', type: 'int'}],
                        tooltipAlarmMsg: Uni.I18n.translate('alarms.alarmsPerReasonAndDay', 'DAL', '{0} of this day: {1} alarms'),
                        url: '/api/dal/history',
                        field: 'openVsClose',
                        translationFields: [
                            {reason: 'open', translation: Uni.I18n.translate('alarms.alarmOpenClosed.open', 'DAL', 'Open')},
                            {reason: 'closed', translation: Uni.I18n.translate('alarms.alarmOpenClosed.closed', 'DAL', 'Closed')}],
                        legendTitle: [Uni.I18n.translate('alarms.alarmOpenClosed.open', 'DAL', 'Open'),
                            Uni.I18n.translate('alarms.alarmOpenClosed.closed', 'DAL', 'Closed')
                        ],
                        defaultColors: ['#eb5642', '#70bb52'],
                        colorPerReason: [
                            {reason: 'open', color: '#eb5642'},
                            {reason: 'closed', color: '#70bb52'}
                        ]
                    }
                ]
            }
        ]
        me.callParent(arguments);
    }
});
