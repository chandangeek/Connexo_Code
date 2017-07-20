Ext.define('Isu.view.overview.HistoryPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.history-issues-panel',
    requires: [
        'Uni.view.widget.HistoryGraph',
        'Isu.view.overview.HistoryFilter',
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.store.IssueStatuses'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'view-issue-history-filter',
                itemId: 'view-issue-history-filter'
            },
            {
                xtype: 'no-issues-found-panel',
                itemId: 'overview-no-history-issues-found-panel',
                hidden: true
            },
            {
                xtype: 'panel',
                itemId: 'overview-graphs-panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesPerReason', 'ISU', 'Issues per reason'),
                        itemId: 'crt-issues-per-reason',
                        margin: '20 10 0 0',
                        showLegend: false,
                        tooltipAlarmMsg: Uni.I18n.translate('issues.issuesPerReason', 'ISU', '{0}: {1} issues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalIssuesRaised', 'ISU', 'Total issues raised on this day: {0}'),
                        url: '/api/isu/history',
                        field: 'reasonsPerDay',
                        traslationStore: 'Isu.store.IssueReasons'
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesOpenClosed', 'ISU', 'Issues open vs closed'),
                        itemId: 'crt-issues-open-closed',
                        margin: '20 10 0 0',
                        defaultFields: ['open', 'closed'],
                        defaultFields2: [{name: 'open', type: 'int'}, {name: 'closed', type: 'int'}],
                        tooltipAlarmMsg: Uni.I18n.translate('issues.issuesPerReasonAndDay', 'ISU', '{0} of this day: {1} isues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalIssuesRaised', 'ISU', 'Total issues raised on this day: {0}'),
                        url: '/api/isu/history',
                        field: 'openVsClose',
                        translationFields: [
                            {reason: 'open', translation: Uni.I18n.translate('issues.issueOpenClosed.open', 'ISU', 'Open')},
                            {reason: 'closed', translation: Uni.I18n.translate('issues.issueOpenClosed.closed', 'ISU', 'Closed')}
                        ],
                        legendTitle: [
                            Uni.I18n.translate('issues.issueOpenClosed.open', 'ISU', 'Open'),
                            Uni.I18n.translate('issues.issueOpenClosed.closed', 'ISU', 'Closed')
                        ],
                        defaultColors: ['#eb5642', '#70bb52'],
                        colorPerReason: [
                            {reason: 'open', color: '#eb5642'},
                            {reason: 'closed', color: '#70bb52'}
                        ]
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesPerPriority', 'ISU', 'Issues per priority'),
                        itemId: 'crt-issues-per-priority',
                        margin: '20 10 0 0',
                        showLegend: true,
                        tooltipAlarmMsg: Uni.I18n.translate('issues.issuesPerPriority', 'ISU', '{0}: {1} issues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('alarms.totalIssuesRaised', 'ISU', 'Total issues raised on this day: {0}'),
                        url: '/api/isu/history',
                        field: 'priorityPerDay',
                        translationFields: [
                            {reason: 'veryLow', translation: Uni.I18n.translate('issue.priority.veryLow', 'ISU', 'Very low')},
                            {reason: 'low', translation: Uni.I18n.translate('issue.priority.low', 'ISU', 'Low')},
                            {reason: 'medium', translation: Uni.I18n.translate('issue.priority.medium', 'ISU', 'Medium')},
                            {reason: 'high', translation: Uni.I18n.translate('issue.priority.high', 'ISU', 'High')},
                            {reason: 'veryHigh', translation: Uni.I18n.translate('issue.priority.veryHigh', 'ISU', 'Very high')}
                        ],
                        legendTitle: [
                            Uni.I18n.translate('issue.priority.veryLow', 'ISU', 'Very low'),
                            Uni.I18n.translate('issue.priority.low', 'ISU', 'Low'),
                            Uni.I18n.translate('issue.priority.medium', 'ISU', 'Medium'),
                            Uni.I18n.translate('issue.priority.high', 'ISU', 'High'),
                            Uni.I18n.translate('issue.priority.veryHigh', 'ISU', 'Very high')
                        ],
                        defaultColors: ['#70BB51', '#568343', '#71adc7', '#dedc49', '#eb5642'],
                        colorPerReason: [
                            {reason: 'veryLow', color: '#70BB51'},
                            {reason: 'low', color: '#568343'},
                            {reason: 'medium', color: '#71adc7'},
                            {reason: 'high', color: '#dedc49'},
                            {reason: 'veryHigh', color: '#eb5642'}
                        ],
                        prepareColorsAndFields: function (fields) {
                            var me = this, sortedFileds = [];

                            me.translationFields = [];
                            me.legendTitle = [];
                            me.defaultColors = [];
                            me.colorPerReason = [];

                            Ext.Array.contains(fields, 'veryLow')
                            && sortedFileds.push('veryLow')
                            && me.colorPerReason.push({reason: 'veryLow', color: '#70BB51'})
                            && me.legendTitle.push(Uni.I18n.translate('issue.priority.veryLow', 'ISU', 'Very low'))
                            && me.translationFields.push({reason: 'veryLow', translation: Uni.I18n.translate('issue.priority.veryLow', 'ISU', 'Very low')})
                            && me.defaultColors.push('#70BB51');

                            Ext.Array.contains(fields, 'low')
                            && sortedFileds.push('low')
                            && me.colorPerReason.push({reason: 'low', color: '#568343'})
                            && me.legendTitle.push(Uni.I18n.translate('issue.priority.low', 'ISU', 'Low'))
                            && me.translationFields.push({reason: 'low', translation: Uni.I18n.translate('issue.priority.low', 'ISU', 'Low')})
                            && me.defaultColors.push('#568343');

                            Ext.Array.contains(fields, 'medium')
                            && sortedFileds.push('medium')
                            && me.colorPerReason.push({reason: 'medium', color: '#71adc7'})
                            && me.legendTitle.push(Uni.I18n.translate('issue.priority.medium', 'ISU', 'Medium'))
                            && me.translationFields.push({reason: 'medium', translation: Uni.I18n.translate('issue.priority.medium', 'ISU', 'Medium')})
                            && me.defaultColors.push('#71adc7');

                            Ext.Array.contains(fields, 'high')
                            && sortedFileds.push('high')
                            && me.colorPerReason.push({reason: 'high', color: '#dedc49'})
                            && me.legendTitle.push(Uni.I18n.translate('issue.priority.high', 'ISU', 'High'))
                            && me.translationFields.push({reason: 'high', translation: Uni.I18n.translate('issue.priority.high', 'ISU', 'High')})
                            && me.defaultColors.push('#dedc49');

                            Ext.Array.contains(fields, 'veryHigh')
                            && sortedFileds.push('veryHigh')
                            && me.colorPerReason.push({reason: 'veryHigh', color: '#eb5642'})
                            && me.legendTitle.push(Uni.I18n.translate('issue.priority.veryHigh', 'ISU', 'Very high'))
                            && me.translationFields.push({reason: 'veryHigh', translation: Uni.I18n.translate('issue.priority.veryHigh', 'ISU', 'Very high')})
                            && me.defaultColors.push('#eb5642');

                            return sortedFileds;
                        }
                    }
                ]
            }
        ]
        me.callParent(arguments);
    }
});
