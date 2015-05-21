Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-device-life-cycle-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                itemId: 'change-device-life-cycle-combo',
                name: 'deviceLifeCycleId',
                width: 500,
                labelWidth: 250,
                store: 'Mdc.store.DeviceLifeCycles',
                required: true,
                editable: false,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id'
            }
        ];

        me.callParent(arguments);
    }
});