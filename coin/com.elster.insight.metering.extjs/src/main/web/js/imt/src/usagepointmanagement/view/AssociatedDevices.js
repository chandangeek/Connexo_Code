Ext.define('Imt.usagepointmanagement.view.AssociatedDevices', {
    extend: 'Ext.form.Panel',
    alias: 'widget.associated-devices',
    itemId: 'associated-devices',
    title: Uni.I18n.translate('usagePointManagement.associateddevices', 'IMT', 'Associated devices'),
    router: null,
    ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 125,
                fieldLabel: Uni.I18n.translate('usagePointManagement.linkedDevices', 'IMT', 'Linked device'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'associatedDevicesLinked',
                items: []
            },
            {
                xtype: 'menuseparator',
                itemId: 'associatedDevicesSeparator',
                margin: '0 0 20px 0',
                hidden: true
            },
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 125,
                fieldLabel: Uni.I18n.translate('usagePointManagement.history', 'IMT', 'history'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'associatedDevicesHistory',
                hidden: true,
                items: []
            },

        ];
        me.callParent(arguments);
    }
});