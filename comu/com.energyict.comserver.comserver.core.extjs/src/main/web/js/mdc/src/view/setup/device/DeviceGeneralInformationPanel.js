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
            },
            {
                xtype: 'button',
                margin: '0 0 0 65',
                text: Uni.I18n.translate('deviceGeneralInformation.viewmorelinktext', 'MDC', 'View more'),
                ui: 'link',
                itemId: 'view-more-general-information-link',
                handler: function() {
                    me.router.getRoute('devices/device/attributes').forward();
                }
            }
        ];

        me.callParent(arguments);
    }
});

