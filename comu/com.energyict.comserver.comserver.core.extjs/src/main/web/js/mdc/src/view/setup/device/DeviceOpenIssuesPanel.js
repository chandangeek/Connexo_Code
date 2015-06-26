Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('deviceOpenIssues.openIssuesTitle', 'MDC', 'Open issues'),
    //layout: 'vbox',

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
                status: ['status.open', 'status.in.progress'],
                meter: mRID,
                sorting: [
                    {
                        type: 'dueDate',
                        value: 'asc'
                    }
                ]
            }
        };

        me.down('#dataCollectionIssuesContainer').add({
            xtype: 'button',
            text: Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssuesOnMeter', value, 'MDC', '{0} data collection issues'),
            ui: 'link',
            href: typeof me.router.getRoute('workspace/datacollectionissues') !== 'undefined'
                ? me.router.getRoute('workspace/datacollectionissues').buildUrl(null, assignedFilter) : null
        });
    }
})
;

