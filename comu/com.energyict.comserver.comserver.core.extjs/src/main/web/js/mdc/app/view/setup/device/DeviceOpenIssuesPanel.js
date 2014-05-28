Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    deviceId: null,
    mRID: null,
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
                        if (value !== 0) {
                        return '<a href="#/workspace/datacollection/issues?issueType=datacollection&group=none&status=1&meter=' + me.mRID + '" >' + Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssues',value, 'MDC', 'issues') + '</a>';
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

