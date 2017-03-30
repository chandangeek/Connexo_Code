/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegistersTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-registers-overview-topfilter',

    requires: [
        'Mdc.store.filter.RegisterGroups',
        'Mdc.store.filter.RegistersOfDeviceForRegisterGroups'
    ],

    store: 'Mdc.store.RegisterConfigsOfDevice',
    deviceId: null,

    initComponent: function () {
        var me = this,
            registerGroupsStore = Ext.getStore('Mdc.store.filter.RegisterGroups') || Ext.create('Mdc.store.filter.RegisterGroups'),
            registerStore = Ext.getStore('Mdc.store.filter.RegistersOfDeviceForRegisterGroups') || Ext.create('Mdc.store.filter.RegistersOfDeviceForRegisterGroups');

        registerGroupsStore.getProxy().setUrl(me.deviceId);
        registerStore.getProxy().setUrl(me.deviceId);

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
    }

});