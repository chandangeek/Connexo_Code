Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-device-life-cycle-step2',
    html: '',
    margin: '0 0 15 0',
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                margin: 0,
                padding: 10,
                itemId: 'change-device-life-cycle-failed',
                hidden: true,
                layout: {
                    type: 'hbox',
                    defaultMargins: '5 10 5 5'
                },
                buttonAlign: 'left',
                buttons: [
                    {
                        text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                        margin: '0 0 0 46',
                        ui: 'action',
                        action: 'finish',
                        itemId: 'change-device-life-cycle-finish-failed',
                        hidden: true,
                        href: me.router.getRoute('administration/devicetypes').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },
    setResultMessage: function (result, success) {
        var me = this;

        if (success) {
            me.update('<h3>' + Uni.I18n.translate('deviceLifeCycle.change.successMsg', 'MDC', 'Successfully changed device life cycle') + '</h3>');
        } else {
            me.down('#change-device-life-cycle-failed').show();
            me.down('#change-device-life-cycle-failed').setText();
        }
    }
});