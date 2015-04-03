Ext.define('Dlc.devicelifecycles.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-add',
    router: null,
    requires: ['Dlc.devicelifecycles.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'device-life-cycles-add-form',
                title: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                infoText: Uni.I18n.translate('deviceLifeCycles.add.templateMsg', 'DLC', 'The new device life cycle is based on the standard template and will use the same states and transitions.'),
                router: me.router,
                btnAction: 'add',
                btnText: Uni.I18n.translate('general.add', 'DLC', 'Add')
            }
        ];
        me.callParent(arguments);
    }
});
