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
                text: Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssuesOnMeter', device.get('nbrOfDataCollectionIssues'), 'MDC', '{0} data collection issues'),
                ui: 'link',
                href: typeof me.router.getRoute('workspace/datacollectionissues') !== 'undefined'
                    ? me.router.getRoute('workspace/datacollectionissues').buildUrl(null, assignedFilter) : null
            });

        me.down('#dataValidationIssuesContainer').add(
            {
                xtype: 'button',
                text: Uni.I18n.translatePlural('deviceOpenIssues.dataValidationIssuesOnMeter', device.get('nbrOfDataValidationIssues'), 'MDC', '{0} data validation issues'),
                ui: 'link',
                href: typeof me.router.getRoute('workspace/datavalidationissues') !== 'undefined'
                    ? me.router.getRoute('workspace/datavalidationissues').buildUrl(null, assignedFilter) : null
            });
    }
})
;

