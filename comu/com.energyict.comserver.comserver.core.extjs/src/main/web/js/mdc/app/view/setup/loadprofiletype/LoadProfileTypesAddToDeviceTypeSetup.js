Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypesAddToDeviceTypeSetup',
    itemId: 'loadProfileTypesAddToDeviceTypeSetup',

    intervalStore: null,
    deviceTypeId: null,

    content: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            ui: 'large',
            title: Uni.I18n.translate('loadprofiletype.addloadprofiletypes', 'MDC', 'Add load profile types')
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.down('panel').add(
            {
                xtype: 'loadProfileTypesAddToDeviceTypeGrid',
                intervalStore: me.intervalStore,
                deviceTypeId: me.deviceTypeId
            }
        );
    }
});

