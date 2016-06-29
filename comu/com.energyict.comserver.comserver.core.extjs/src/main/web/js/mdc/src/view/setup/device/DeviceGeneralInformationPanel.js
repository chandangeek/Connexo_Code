Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    title: Uni.I18n.translate('deviceGeneralInformation.title', 'MDC', 'Device summary'),
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
                text: Uni.I18n.translate('deviceGeneralInformation.viewMoreLinkText', 'MDC', 'More attributes'),
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

