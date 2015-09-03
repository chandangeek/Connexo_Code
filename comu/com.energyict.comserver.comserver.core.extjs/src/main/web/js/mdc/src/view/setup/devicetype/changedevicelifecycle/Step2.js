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
                htmlEncode: false,
                margin: 0,
                padding: 10,
                itemId: 'change-device-life-cycle-failed',
                hidden: true,
                layout: {
                    type: 'hbox',
                    defaultMargins: '5 10 5 5'
                }
            }
        ];

        me.callParent(arguments);
    },
    setResultMessage: function (result, success) {
        var me = this;

        if (success) {
            me.update('<h3>' + Uni.I18n.translate('deviceLifeCycle.change.successMsg', 'MDC', 'Successfully changed device life cycle') + '</h3>');
        } else {
            var states = '';
            Ext.Array.each(result.notMappableStates, function (state) {
                states += '<li style="margin-left: 20px">' + state.name + '</li>';
            });
            me.down('#change-device-life-cycle-failed').setText(Uni.I18n.translate('deviceLifeCycle.change.errorMsg1', 'MDC', '{0} has states that cannot be mapped to states of {1}',
                ['<h3>' + result.message + '</h3><br><a href="#/administration/devicelifecycles/' + result.currentDeviceLifeCycle.id + '">' + result.currentDeviceLifeCycle.name + '</a>',
                    '<a href="#/administration/devicelifecycles/' + result.targetDeviceLifeCycle.id + '">' + result.targetDeviceLifeCycle.name + '</a>']) +
                + Uni.I18n.translate('deviceLifeCycle.change.errorMsg2', 'MDC', 'and there are devices in that states:') + '<br>' + states);
            me.down('#change-device-life-cycle-failed').show();
        }
    }
});