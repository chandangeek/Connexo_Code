/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.StaticGroupDevicesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.static-group-devices-grid',
    store: 'Mdc.store.StaticGroupDevices',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.store.search.Results',
        'Uni.view.search.ColumnPicker',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfDevices.selected', count, 'MDC',
            'No devices selected', '{0} device selected', '{0} devices selected'
        );
    },

    allLabel: Uni.I18n.translate('deviceGroup.bulk.allDevices', 'MDC', 'All devices'),
    allDescription: Uni.I18n.translate('deviceGroup.bulk.selectMsg', 'MDC', 'Select all devices (according to search criteria)'),

    selectedLabel: Uni.I18n.translate('deviceGroup.bulk.selectedDevices', 'MDC', 'Selected devices'),
    selectedDescription: Uni.I18n.translate('deviceGroup.bulk.selectedDevicesInTable', 'MDC', 'Select devices in table'),

    bottomToolbarHidden: true,

    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    listeners: {
        afterrender: {
            fn: function () {
                var me = this;

                me.down('#topToolbarContainer').insert(3, '->');
                me.down('#topToolbarContainer').insert(4, {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'uni-search-column-picker',
                            itemId: 'static-column-picker',
                            grid: me
                        }
                    ]
                });
            }
        }
    },

    devices: null,
    listenersAreInited: false,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.initListeners();
    },

    setDevices: function (devices) {
        var me = this,
            ids = [];

        Ext.Array.each(devices, function (device) {
            ids.push(device.get('id'));
        });
        me.devices = ids;
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
    },

    initListeners: function () {
        var me = this;

        if (me.listenersAreInited) {
            return
        }
        me.un('selectionchange', me.onSelectionChange, me);
        me.on('select', me.onSelect, me);
        me.on('beforedeselect', me.onBeforeDeselect, me);
        me.getStore().on('prefetch', me.onPrefetch, me);
        me.on('destroy', function () {
            me.un('select', me.onSelect, me);
            me.un('beforedeselect', me.onBeforeDeselect, me);
            me.getStore().un('prefetch', me.onPrefetch, me);
        });
        me.listenersAreInited = true;
    },

    onSelect: function (selectionModel, record) {
        var me = this;

        if (!me.devices) {
            me.devices = [];
        }
        Ext.Array.include(me.devices, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
        Ext.resumeLayouts(true);
    },

    onBeforeDeselect: function (selectionModel, record) {
        var me = this;

        Ext.Array.remove(me.devices, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
        Ext.resumeLayouts(true);
    },

    onPrefetch: function (store, records) {
        var me = this,
            selectionModel = me.getSelectionModel(),
            toSelect = [];

        if (Ext.isArray(me.devices)) {
            Ext.Array.each(records, function (record) {
                if (Ext.Array.contains(me.devices, record.get('id'))) {
                    toSelect.push(record);
                }
            });
        }

        if (toSelect.length) {
            selectionModel.select(toSelect, true, true);
        }
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        Ext.suspendLayouts();
        me.getSelectionModel().deselectAll();
        button.disable();
        me.getSelectionCounter().setText(me.counterTextFn(0));
        Ext.resumeLayouts(true);
        if (me.devices) {
            me.devices = [];
        }
    }
});