Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.purposes-preview',
    router: null,
    title: ' ',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                itemId: 'purposes-preview-container',
                fieldLabel: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                layout: 'vbox'
            }
        ];
        me.callParent(arguments);
    }
});


