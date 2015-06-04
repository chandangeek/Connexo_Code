Ext.define('Fwc.firmwarecampaigns.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.firmwarecampaigns.view.AddForm'
    ],
    alias: 'widget.firmware-campaigns-add',
    returnLink: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'firmware-campaigns-add-form',
                itemId: 'firmware-campaigns-add-form',
                title: Uni.I18n.translate('firmware.campaigns.addFirmwareCampaign', 'FWC', 'Add firmware campaign'),
                ui: 'large',
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});