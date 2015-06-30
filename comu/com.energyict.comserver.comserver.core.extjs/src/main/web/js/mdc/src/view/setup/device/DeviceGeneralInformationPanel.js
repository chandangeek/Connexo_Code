Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    title: Uni.I18n.translate('deviceGeneralInformation.generalInformationTitle', 'MDC', 'General information'),
    ui: 'tile',
    router: null,

    requires: [
      'Mdc.view.setup.device.DeviceAttributesForm'
    ],

    initComponent: function() {
        var me = this;

        me.items = [
            {
                xtype: 'deviceAttributesForm',
                itemId: 'deviceGeneralInformationForm',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});

