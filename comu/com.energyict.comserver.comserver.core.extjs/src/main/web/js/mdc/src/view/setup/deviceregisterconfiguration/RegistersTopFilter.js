Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegistersTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-registers-overview-topfilter',

    requires: [
        'Mdc.store.filter.RegisterGroups',
        'Mdc.store.filter.RegistersOfDeviceForRegisterGroups'
    ],

    store: 'Mdc.store.RegisterConfigsOfDevice',
    deviceMRID: null,

    initComponent: function () {
        var me = this,
            registerGroupsStore = Ext.getStore('Mdc.store.filter.RegisterGroups') || Ext.create('Mdc.store.filter.RegisterGroups'),
            registerStore = Ext.getStore('Mdc.store.filter.RegistersOfDeviceForRegisterGroups') || Ext.create('Mdc.store.filter.RegistersOfDeviceForRegisterGroups');

        registerGroupsStore.getProxy().setUrl(me.deviceMRID);
        registerStore.getProxy().setUrl(me.deviceMRID);

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'groups',
                emptyText: Uni.I18n.translate('general.registerGroup', 'MDC', 'Register group'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: registerGroupsStore,
                itemId: 'mdc-register-group-filter',
                listeners: {
                    collapse: {
                        scope: me,
                        fn: me.onRegisterGroupComboChange
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'registers',
                emptyText: Uni.I18n.translate('general.register', 'MDC', 'Register'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: registerStore,
                itemId: 'mdc-register-filter'
            }
        ];

        me.callParent(arguments);
    },

    onRegisterGroupComboChange: function() {
        var me = this,
            groupCombo = me.down('#mdc-register-group-filter'),
            registerCombo = me.down('#mdc-register-filter'),
            registerStore = registerCombo.getStore();

        registerStore.getProxy().setExtraParam('filter', Ext.encode([{
            property: 'groups',
            value: groupCombo.getValue()
        }]));
        registerStore.load(function () {
            registerStore.sort('name', 'ASC');
            registerCombo.select(registerCombo.getValue()); // restore previous selection(s)
        });
    }
});