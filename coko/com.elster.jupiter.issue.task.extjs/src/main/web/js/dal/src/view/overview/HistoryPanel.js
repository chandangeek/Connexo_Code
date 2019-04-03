Ext.define('Itk.view.overview.HistoryPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.history-panel',
    requires: [
        'Uni.view.widget.HistoryGraph',
        'Itk.view.overview.HistoryFilter',
        'Itk.view.NoIssuesFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'view-history-filter',
                itemId: 'view-history-filter'
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
                //        hidden: true,
                items: [
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesPerReason', 'ITK', 'Issues per reason'),
                        itemId: 'crt-issues-per-reason',
                        margin: '20 10 0 0',
                        showLegend: false,
                        tooltipIssueMsg: Uni.I18n.translate('issues.issuesPerReason', 'ITK', '{0}: {1} issues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('issues.totalIssuesRaised', 'ITK', 'Total issues raised on this day: {0}'),
                        url: '/api/itk/history',
                        field: 'reasonsPerDay'
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesOpenClosed', 'ITK', 'Issues open vs closed'),
                        itemId: 'crt-issues-open-closed',
                        margin: '20 10 0 0',
                        defaultFields: ['open', 'closed'],
                        defaultFields2: [{name: 'open', type: 'int'}, {name: 'closed', type: 'int'}],
                        tooltipIssueMsg: Uni.I18n.translate('issues.issuesPerReasonAndDay', 'ITK', '{0} of this day: {1} issues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('issues.totalIssuesRaised', 'ITK', 'Total issues raised on this day: {0}'),
                        url: '/api/itk/history',
                        field: 'openVsClose',
                        translationFields: [
                            {reason: 'open', translation: Uni.I18n.translate('issues.issueOpenClosed.open', 'ITK', 'Open')},
                            {reason: 'closed', translation: Uni.I18n.translate('issues.issueOpenClosed.closed', 'ITK', 'Closed')}],
                        legendTitle: [Uni.I18n.translate('issues.issueOpenClosed.open', 'ITK', 'Open'),
                            Uni.I18n.translate('issues.issueOpenClosed.closed', 'ITK', 'Closed')
                        ],
                        defaultColors: ['#eb5642', '#70bb52'],
                        colorPerReason: [
                            {reason: 'open', color: '#eb5642'},
                            {reason: 'closed', color: '#70bb52'}
                        ]
                    },
                    {
                        xtype: 'history-graph',
                        title: Uni.I18n.translate('workspace.issuesPerPriority', 'ITK', 'Issues per priority'),
                        itemId: 'crt-issues-per-priority',
                        margin: '20 10 0 0',
                        showLegend: true,
                        tooltipIssueMsg: Uni.I18n.translate('issues.issuePerPriority', 'ITK', '{0}: {1} issues'),
                        totalItemsRaisedMsg: Uni.I18n.translate('issues.totalIssuesRaised', 'ITK', 'Total issues raised on this day: {0}'),
                        url: '/api/itk/history',
                        field: 'priorityPerDay',
                        translationFields: [
                            {
                                reason: 'veryLow',
                                translation: Uni.I18n.translate('issue.priority.veryLow', 'ITK', 'Very low')
                            },
                            {reason: 'low', translation: Uni.I18n.translate('issue.priority.low', 'ITK', 'Low')},
                            {
                                reason: 'medium',
                                translation: Uni.I18n.translate('issue.priority.medium', 'ITK', 'Medium')
                            },
                            {reason: 'high', translation: Uni.I18n.translate('issue.priority.high', 'ITK', 'High')},
                            {
                                reason: 'veryHigh',
                                translation: Uni.I18n.translate('issue.priority.veryHigh', 'ITK', 'Very high')
                            }
                        ],
                        legendTitle: [
                            Uni.I18n.translate('issue.priority.veryLow', 'ITK', 'Very low'),
                            Uni.I18n.translate('issue.priority.low', 'ITK', 'Low'),
                            Uni.I18n.translate('issue.priority.medium', 'ITK', 'Medium'),
                            Uni.I18n.translate('issue.priority.high', 'ITK', 'High'),
                            Uni.I18n.translate('issue.priority.veryHigh', 'ITK', 'Very high')
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
