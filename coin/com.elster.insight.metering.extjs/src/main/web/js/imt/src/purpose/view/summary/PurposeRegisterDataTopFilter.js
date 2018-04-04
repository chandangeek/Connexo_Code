/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.PurposeRegisterDataTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.purpose-register-topfilter',

    requires: [
        'Imt.purpose.store.RegisterFilter'
    ],

    filterDefault: {
        from: moment().startOf('day').subtract(7, 'days').toDate(),
        to: moment().endOf('day').toDate()
    },

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'registers',
                emptyText: Uni.I18n.translate('general.register', 'IMT', 'Register'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.purpose.store.RegisterFilter',
                loadStore: false,
                itemId: 'topfilter-registers-combo'
            },
            {
                type: 'interval',
                text: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                dataIndex: 'measurementTime',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to,
                itemId: 'topfilter-time-interval'
            }
        ];

        me.callParent(arguments);
    }
});