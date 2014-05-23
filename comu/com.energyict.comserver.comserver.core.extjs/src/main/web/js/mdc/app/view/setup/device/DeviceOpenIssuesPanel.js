Ext.define('Mdc.view.setup.device.DeviceOpenIssuesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceOpenIssuesPanel',
    overflowY: 'auto',
    itemId: 'deviceopenissuespanel',
    deviceId: null,
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('deviceOpenIssues.openIssuesTitle', 'MDC', 'Open issues'),
    items: [
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
                    xtype: 'component',
                    cls: 'x-form-display-field',
                    autoEl: {
                        tag: 'a',
                        href: '#/workspace/datacollection/issues?issueType=datacollection&group=none&status=1&meter=' + this.mRID,
                        html: 'data collection issues'
                    },
                    itemId: 'dataCollectionIssuesLink'
                }

            ]
        }
    ]
})
;

