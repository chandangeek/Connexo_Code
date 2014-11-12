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

    setDataCollectionIssues: function (value) {
        var me = this,
            mRID = me.router.arguments.mRID,
            assignedFilter;

        assignedFilter = {
            filter: {
                status: 'status.open',
                meter: mRID,
                sorting: [
                    {
                        type: 'dueDate',
                        value: 'asc'
                    }
                ]
            }
        };

        me.down('#dataCollectionIssuesContainer').add(
            {
                xtype: 'button',
                text: Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssuesOnMeter', value, 'MDC', '{0} data collection issues'),
                ui: 'link',
                href: me.router.getRoute('workspace/datacollection/issues').buildUrl(null, assignedFilter)
            });
    }
})
;

