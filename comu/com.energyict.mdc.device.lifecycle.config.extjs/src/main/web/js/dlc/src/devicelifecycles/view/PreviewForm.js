Ext.define('Dlc.devicelifecycles.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycles-preview-form',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name',
                labelWidth: 250
            }
        ];

        me.callParent(arguments);
    }
});
