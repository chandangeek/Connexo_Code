Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-device-life-cycle-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                hidden: true,
                width: 460
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                itemId: 'change-device-life-cycle-combo',
                name: 'deviceLifeCycleId',
                width: 500,
                labelWidth: 150,
                store: 'Mdc.store.DeviceLifeCycles',
                required: true,
                editable: false,
                queryMode: 'local',
                margin: '20 0 20 -40',
                lastQuery: '',
                displayField: 'name',
                valueField: 'id'
            }
        ];

        me.callParent(arguments);
    }
});