Ext.define('Dal.view.overview.HistoryPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.history-panel',
    requires: [
        'Uni.view.widget.HistoryGraph',
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
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalAlarmsRaised', 'DAL', 'Total alarms raised on this day: {0}'),
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
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalAlarmsRaised', 'DAL', 'Total alarms raised on this day: {0}'),
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
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.alarmsPerPriority', 'DAL', 'Alarms per priority'),
                        itemId: 'crt-alarms-per-priority',
                        margin: '20 10 0 0',
                        showLegend: true,
                        tooltipAlarmMsg: Uni.I18n.translate('alarms.alarmPerPriority', 'DAL', '{0}: {1} alarms'),
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalAlarmsRaised', 'DAL', 'Total alarms raised on this day: {0}'),
                        url: '/api/dal/history',
                        field: 'priorityPerDay',
                        translationFields: [
                            {
                                reason: 'veryLow',
                                translation: Uni.I18n.translate('alarm.priority.veryLow', 'DAL', 'Very low')
                            },
                            {reason: 'low', translation: Uni.I18n.translate('alarm.priority.low', 'DAL', 'Low')},
                            {
                                reason: 'medium',
                                translation: Uni.I18n.translate('alarm.priority.medium', 'DAL', 'Medium')
                            },
                            {reason: 'high', translation: Uni.I18n.translate('alarm.priority.high', 'DAL', 'High')},
                            {
                                reason: 'veryHigh',
                                translation: Uni.I18n.translate('alarm.priority.veryHigh', 'DAL', 'Very high')
                            }
                        ],
                        legendTitle: [
                            Uni.I18n.translate('alarm.priority.veryLow', 'DAL', 'Very low'),
                            Uni.I18n.translate('alarm.priority.low', 'DAL', 'Low'),
                            Uni.I18n.translate('alarm.priority.medium', 'DAL', 'Medium'),
                            Uni.I18n.translate('alarm.priority.high', 'DAL', 'High'),
                            Uni.I18n.translate('alarm.priority.veryHigh', 'DAL', 'Very high')
                        ],
                        defaultColors: ['#70BB51', '#568343', '#71adc7', '#dedc49', '#eb5642'],
                        colorPerReason: [
                            {reason: 'veryLow', color: '#70BB51'},
                            {reason: 'low', color: '#568343'},
                            {reason: 'medium', color: '#71adc7'},
                            {reason: 'high', color: '#dedc49'},
                            {reason: 'veryHigh', color: '#eb5642'}
                        ],
                        getFields: function (fields) {
                            return ['veryLow', 'low', 'medium', 'high', 'veryHigh'];
                        }
                    }
                ]
            }
        ]
        me.callParent(arguments);
    }
});
