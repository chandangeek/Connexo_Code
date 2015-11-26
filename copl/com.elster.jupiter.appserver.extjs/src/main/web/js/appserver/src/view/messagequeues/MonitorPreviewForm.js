Ext.define('Apr.view.messagequeues.MonitorPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.monitor-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.messages', 'APR', 'Messages'),
                name: 'numberOfMessages'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.Errors', 'APR', 'Errors'),
                name: 'numberOFErrors'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.messageServiceName', 'APR', 'Message service name'),
                itemId: 'txt-export-path',
                name: 'exportDirectory'
            }
        ];
        me.callParent(arguments);
    }

});
