Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    deviceId: null,
    margin: '0 10 10 10',
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'component',
                html: '<h4>' + Uni.I18n.translate('deviceOpenIssues.openIssuesTitle', 'MDC', 'Open issues') + '</h4>',
                itemId: 'openIssuesTitle'
            },
            {
                xtype: 'form',
                itemId: 'deviceOpenIssuesForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 200
                },
                items: [


                ]
            }
        ];
        this.callParent();
    }
})
;

