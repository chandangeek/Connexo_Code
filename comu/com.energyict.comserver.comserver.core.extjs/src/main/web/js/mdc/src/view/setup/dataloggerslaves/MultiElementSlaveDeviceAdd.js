Ext.define('Mdc.view.setup.dataloggerslaves.MultiElementSlaveDeviceAdd', {
    extend: 'Ext.form.Panel',
    alias: 'widget.multi-element-slave-device-add',
    itemId: 'mdc-multi-element-slave-device-add',
    requires: [
        'Mdc.util.LinkPurpose',
        'Mdc.widget.DeviceConfigurationField'
    ],
    hydrator: 'Uni.util.Hydrator',
    width: 570,
    margin: '20 0 0 0',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 145
    },
    initComponent: function () {
        var me = this,
            deviceTypeStore = Ext.getStore('Mdc.store.AvailableDeviceTypes');

        deviceTypeStore.clearFilter(true);
        deviceTypeStore.filter([
            Ext.create('Ext.util.Filter', {filterFn: Mdc.util.LinkPurpose.properties[ Mdc.util.LinkPurpose.LINK_MULTI_ELEMENT_SLAVE].deviceTypeFilter})
        ]);
        me.items = [{
                xtype: 'deviceConfigurationField',
                itemId: 'multiElementSlaveDeviceConfiguration',
                deviceTypeStore: deviceTypeStore,
                queryMode: 'remote',
                allowBlank: false,
                width: 570
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'multiElementSlaveName',
                fieldLabel: Uni.I18n.translate('deviceAdd.name', 'MDC', 'Name'),
                required: true,
                msgTarget: 'under',
                maxLength: 80,
                enforceMaxLength: true,
                allowBlank: false,
                validateOnBlur: false,
                validateOnChange: false
            }
        ];
        me.callParent(arguments);
    }
});

