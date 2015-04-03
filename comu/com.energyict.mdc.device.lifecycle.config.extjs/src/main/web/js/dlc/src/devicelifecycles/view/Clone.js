Ext.define('Dlc.devicelifecycles.view.Clone', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-clone',
    router: null,
    title: null,
    requires: ['Dlc.devicelifecycles.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'device-life-cycles-add-form',
                title: me.title,
                router: me.router,
                infoText: me.infoText,
                btnAction: 'clone',
                btnText: Uni.I18n.translate('general.clone', 'DLC', 'Clone')
            }
        ];
        me.callParent(arguments);
    }
});