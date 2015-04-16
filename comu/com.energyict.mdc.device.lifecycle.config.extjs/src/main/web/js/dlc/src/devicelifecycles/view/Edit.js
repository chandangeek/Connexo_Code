Ext.define('Dlc.devicelifecycles.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-edit',
    router: null,
    requires: ['Dlc.devicelifecycles.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'device-life-cycles-add-form',
                router: me.router,
                btnAction: 'edit',
                btnText: Uni.I18n.translate('general.save', 'DLC', 'Save'),
                hideInfoMsg: true
            }
        ];
        me.callParent(arguments);
    }
});
