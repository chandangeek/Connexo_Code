Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('deviceOpenIssues.openIssuesTitle', 'MDC', 'Open issues'),

    items: [
        {
            xtype: 'container',
            itemId: 'dataCollectionIssuesContainer'
        },
        {
            xtype: 'container',
            itemId: 'dataValidationIssuesContainer'
        }
    ],

    initComponent: function () {
        this.callParent();
    },

    setDataCollectionIssues: function (device) {
        var me = this,
            mRID = me.router.arguments.mRID,
            openDataValidationIssueId = device.get('openDataValidationIssue'),
            assignedFilter;

        assignedFilter = {
            status: ['status.open', 'status.in.progress'],
            meter: mRID,
            groupingType: 'none',
            sort: ['dueDate', 'modTime']
        };

        me.down('#dataCollectionIssuesContainer').add(
            {
                xtype: 'button',
                text: Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssuesOnMeter', device.get('nbrOfDataCollectionIssues'), 'MDC',
                    'No data collection issues', '{0} data collection issue', '{0} data collection issues'),
                ui: 'link',
                href: typeof me.router.getRoute('workspace/datacollectionissues') !== 'undefined'
                    ? me.router.getRoute('workspace/datacollectionissues').buildUrl(null, assignedFilter) : null
            });

        if (!Ext.isEmpty(openDataValidationIssueId)) {
            me.down('#dataValidationIssuesContainer').add(
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('deviceOpenIssues.dataValidationIssuesOnMeter', 'MDC', 'open data validation issue'),
                    ui: 'link',
                    href: typeof me.router.getRoute('workspace/datavalidationissues/view') !== 'undefined'
                        ? me.router.getRoute('workspace/datavalidationissues/view').buildUrl({issueId: openDataValidationIssueId})
                        : null
                });
        }
    }
})
;

