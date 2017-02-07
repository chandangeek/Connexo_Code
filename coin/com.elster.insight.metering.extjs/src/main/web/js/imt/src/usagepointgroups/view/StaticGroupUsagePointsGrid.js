/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.StaticGroupUsagePointsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.static-group-usagepoints-grid',
    xtype: 'static-group-usagepoints-grid',
    store: 'Imt.usagepointgroups.store.StaticGroupUsagePoints',
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.usagepoints.selected', count, 'IMT', 'No usage points selected', '{0} usage point selected', '{0} usage points selected');
    },

    allLabel: Uni.I18n.translate('usagepointsearch.bulkSelection.allLabel', 'IMT', 'All usage points'),
    allDescription: Uni.I18n.translate('usagepointsearch.bulk.selectMsg', 'IMT', 'Select all usage points (according to search criteria)'),
    selectedLabel: Uni.I18n.translate('usagepointsearch.bulkSelection.selectedLabel', 'IMT', 'Selected usage points'),
    selectedDescription: Uni.I18n.translate('usagepointsearch.BulkSelection.selectedDescription', 'IMT', 'Select usage points in table'),
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
                            itemId: 'usagepoints-static-column-picker',
                            grid: me
                        }
                    ]
                });
            }
        }
    },

    usagePoints: null,
    listenersAreInited: false,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.initListeners();
    },

    setUsagePoints: function (usagePoints) {
        var me = this,
            ids = [];

        Ext.Array.each(usagePoints, function (usagePoint) {
            ids.push(usagePoint.get('id'));
        });
        me.usagePoints = ids;
        me.getSelectionCounter().setText(me.counterTextFn(me.usagePoints.length));
        me.getUncheckAllButton().setDisabled(me.usagePoints.length === 0);
    },

    initListeners: function () {
        var me = this;

        if (me.listenersAreInited) {
            return;
        }
        me.un('selectionchange', me.onSelectionChange, me);
        me.on('select', me.onSelect, me);
        me.on('beforedeselect', me.onBeforeDeselect, me);
        me.getStore().on('prefetch', me.onPrefetch, me);
        me.getStore().on('load', function () {me.setLoading(false);}, me);
        me.on('destroy', function () {
            me.un('select', me.onSelect, me);
            me.un('beforedeselect', me.onBeforeDeselect, me);
            me.getStore().un('prefetch', me.onPrefetch, me);
        });
        me.listenersAreInited = true;
    },

    onSelect: function (selectionModel, record) {
        var me = this;

        if (!me.usagePoints) {
            me.usagePoints = [];
        }
        Ext.Array.include(me.usagePoints, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.usagePoints.length));
        me.getUncheckAllButton().setDisabled(me.usagePoints.length === 0);
        Ext.resumeLayouts(true);
    },

    onBeforeDeselect: function (selectionModel, record) {
        var me = this;

        Ext.Array.remove(me.usagePoints, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.usagePoints.length));
        me.getUncheckAllButton().setDisabled(me.usagePoints.length === 0);
        Ext.resumeLayouts(true);
    },

    onPrefetch: function (store, records) {
        var me = this,
            selectionModel = me.getSelectionModel(),
            toSelect = [];

        if (Ext.isArray(me.usagePoints)) {
            Ext.Array.each(records, function (record) {
                if (Ext.Array.contains(me.usagePoints, record.get('id'))) {
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
        if (me.usagePoints) {
            me.usagePoints = [];
        }
    }
});