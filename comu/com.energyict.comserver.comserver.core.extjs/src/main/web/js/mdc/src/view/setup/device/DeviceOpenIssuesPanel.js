Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    mRID: null,
    router: null,
    ui: 'tile',
    title: Uni.I18n.translate('deviceOpenIssues.openIssuesTitle', 'MDC', 'Open issues'),
    initComponent: function () {
        var me = this;
        this.items = [
        {
            xtype: 'form',
            itemId: 'deviceOpenIssuesForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                labelWidth: 150
            },
            items: [
                {
                    name: 'issues',
                    xtype: 'displayfield',
                    itemId: 'dataCollectionIssuesLink',
                    fieldLabel: Uni.I18n.translate('deviceOpenIssues.dataCollectionIssuesTitle', 'MDC', 'Data collection issues'),
                    renderer: function(value, field) {
                        var url = me.router.getRoute('workspace/datacollection/issues').buildUrl({}, {
                            filter: {
                                status: 'status.open',
                                meter: me.mRID,
                                sorting: [
                                    {
                                        type: 'dueDate',
                                        value: 'asc'
                                    }
                                ]
                            }
                        });
                        if (value !== 0) {
                        return '<a href="' + url + '" >' + Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssues',value, 'MDC', 'issues') + '</a>';
                        } else {
                            return Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssues',0, 'MDC', 'issues');
                        }
                    }
                }
            ]
        }
        ];
        this.callParent();
    }
})
;

