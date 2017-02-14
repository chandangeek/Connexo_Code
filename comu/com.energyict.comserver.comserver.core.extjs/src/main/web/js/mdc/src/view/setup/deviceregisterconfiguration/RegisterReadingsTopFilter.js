Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-registerReadings-overview-topfilter',

    requires: [
        'Mdc.store.filter.RegisterGroups',
        'Mdc.store.filter.RegistersOfDeviceForRegisterGroups'
    ],

    store: 'Mdc.store.RegisterReadings',
    deviceId: null,
    containsBillingRegisters: false,
    containsCumulativeRegisters: false,
    containsEventRegisters: false,
    containsMultiplierRegisters: false,

    initComponent: function () {
        var me = this,
            registerGroupsStore = Ext.getStore('Mdc.store.filter.RegisterGroups') || Ext.create('Mdc.store.filter.RegisterGroups'),
            registerStore = Ext.getStore('Mdc.store.filter.RegistersOfDeviceForRegisterGroups') || Ext.create('Mdc.store.filter.RegistersOfDeviceForRegisterGroups');

        registerGroupsStore.getProxy().setUrl(me.deviceId);
        registerStore.getProxy().setUrl(me.deviceId);
        registerStore.on('load', function (store, records) {
            Ext.Array.forEach(records, function (record) {
                me.containsBillingRegisters = me.containsBillingRegisters || record.get('isBilling');
                me.containsCumulativeRegisters = me.containsCumulativeRegisters || record.get('isCumulative');
                me.containsEventRegisters = me.containsEventRegisters || record.get('hasEvent');
                me.containsMultiplierRegisters = me.containsMultiplierRegisters || record.get('useMultiplier');
            });
            me.showOrHideToTimeFilter(me.containsBillingRegisters);
            me.customizeGrid(me.containsBillingRegisters,me.containsCumulativeRegisters,me.containsEventRegisters,me.containsMultiplierRegisters)
        }, me, {single: true});

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
                        fn: me.onRegisterGroupComboCollapse
                    },
                    change: {
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
            },
            {
                type: 'interval',
                text: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'measurementTime',
                dataIndexFrom: 'measurementTimeStart',
                dataIndexTo: 'measurementTimeEnd',
                itemId: 'mdc-measurement-time-filter'
            },
            {
                type: 'interval',
                text: Uni.I18n.translate('general.toTime', 'MDC', 'To time'),
                dataIndex: 'toTime',
                dataIndexFrom: 'toTimeStart',
                dataIndexTo: 'toTimeEnd',
                itemId: 'mdc-to-time-filter',
                hidden: !me.containsBillingRegisters
            }
        ];

        me.callParent(arguments);
    },

    onRegisterGroupComboCollapse: function () {
        var me = this,
            groupCombo = me.down('#mdc-register-group-filter'),
            registerCombo = me.down('#mdc-register-filter'),
            registerStore = registerCombo.getStore();

        registerStore.getProxy().setExtraParam('filter', Ext.encode([{
            property: 'groups',
            value: groupCombo.getValue()
        }]));
        registerStore.load(function () {
            registerCombo.select(registerCombo.getValue()); // restore previous selection(s)
            me.containsBillingRegisters = false;
            me.containsCumulativeRegisters = false;
            me.containsEventRegisters = false;
            me.containsMultiplierRegisters = false;
            registerStore.each(function (record) {
                me.containsBillingRegisters = me.containsBillingRegisters || record.get('isBilling');
                me.containsCumulativeRegisters = me.containsCumulativeRegisters || record.get('isCumulative');
                me.containsEventRegisters = me.containsEventRegisters || record.get('hasEvent');
                me.containsMultiplierRegisters = me.containsMultiplierRegisters || record.get('useMultiplier');
            });
            me.showOrHideToTimeFilter(me.containsBillingRegisters);
        });
    },

    onRegisterGroupComboChange: function (combo) {
        if (combo.isExpanded) {
            return; // if expanded, the collapse trigger will do
        }
        this.onRegisterGroupComboCollapse();
    },

    applyFilters: function () {
        var me = this,
            groupCombo = me.down('#mdc-register-group-filter');

        if (groupCombo.isExpanded) {
            groupCombo.collapse();
            Ext.defer(function () {
                me.callParent(arguments);
            }, 250);
        } else {
            me.callParent(arguments);
        }
    },

    showOrHideToTimeFilter: function (showIt) {
        var toTimeFilter = this.down('#mdc-to-time-filter');
        if (!toTimeFilter) {
            return;
        }
        if (showIt) {
            toTimeFilter.show();
        } else {
            toTimeFilter.hide();
        }
    },

    customizeGrid: function(billing,cumulative,event,multiplier){
        // Also show/hide the corresponding columns in the grid
        var correspondinGrid = this.up('deviceRegisterReadingsView').down('deviceRegisterReadingsGrid');
        if (correspondinGrid) {
            correspondinGrid.customizeColumns(billing,cumulative,event,multiplier);
        }
    }
});