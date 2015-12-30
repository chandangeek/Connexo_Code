Ext.define('Apr.view.messagequeues.MonitorPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.monitor-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250,
        width: 1000
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('general.messages', 'APR', 'Messages'),
            name: 'numberOfMessages'
        },
        {
            fieldLabel: Uni.I18n.translate('general.Errors', 'APR', 'Errors'),
            name: 'numberOFErrors'
        },
        {
            fieldLabel: Uni.I18n.translate('messageQueue.subscribers', 'APR', 'Used by'),
            xtype: 'fieldcontainer',
            itemId: 'used-by-field-container',
            labelWidth: 100
        }
    ],

    customLoadRecord: function(record) {
        var me = this,
            fieldContainer = me.down('#used-by-field-container'),
            internalFieldConainer,
            active;

        Ext.suspendLayouts();
        fieldContainer.removeAll();
        me.loadRecord(record);
        Ext.Array.each(record.get('subscriberSpecInfos'), function (subscriberSpecInfo) {
            internalFieldConainer = {
                xtype: 'fieldcontainer',
                margin: '15 0 0 0',
                labelWidth: 140,
                fieldLabel: subscriberSpecInfo.displayName,
                items: []
            };

            Ext.Array.each(subscriberSpecInfo.appServers, function (appServer) {
                if (appServer.active) {
                    active = Uni.I18n.translate('general.active', 'APR', 'Active')
                } else {
                    active = Uni.I18n.translate('general.inactive', 'APR', 'Inactive')
                }

                internalFieldConainer.items.push({
                    xtype: 'displayfield',
                    value: appServer.appServerName + ' (' + active + ')'
                })
            });
            fieldContainer.add(internalFieldConainer);
        });
        Ext.resumeLayouts(true);
    }
});
