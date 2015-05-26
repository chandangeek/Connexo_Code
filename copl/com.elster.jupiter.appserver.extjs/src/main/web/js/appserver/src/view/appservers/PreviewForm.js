Ext.define('Apr.view.appservers.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.appservers-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.name', 'UNI', 'Name'),
                name: 'name'
            },
            {
                fieldLabel: Uni.I18n.translate('general.status', 'UNI', 'Status'),
                name: 'status'
            },
            {
                fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                itemId: 'export-path',
                name: 'exportPath'
            },
            {
                fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                name: 'messageServices',
                htmlEncode: false
            }
        ];
        me.callParent(arguments);
    }
});
