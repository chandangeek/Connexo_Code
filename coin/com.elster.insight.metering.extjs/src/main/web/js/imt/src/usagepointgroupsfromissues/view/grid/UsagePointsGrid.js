/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.grid.UsagePointsGrid', {

    extend: 'Uni.view.grid.SelectionGrid',

    alias: 'widget.usage-points-grid',

    store: 'Imt.usagepointgroupsfromissues.store.SearchResults',

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
            'usagepointgroupfromissues.wizard.step.selectUsagePoints.grid.selectionInfo', count, 'IMT',
            'No usage points selected', '{0} usage point selected', '{0} usage points selected'
        );
    },

    checkAllButtonPresent: true,

    forceFit: true,

    enableColumnMove: true,

    columns: [],

    config: {
        service: null
    },

    isGridDataLoaded: false,

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

    issues: null,

    listenersAreInited: false,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.initListeners();
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
        me.on('destroy', function () {
            me.un('select', me.onSelect, me);
            me.un('beforedeselect', me.onBeforeDeselect, me);
            me.getStore().un('prefetch', me.onPrefetch, me);
        });
        me.listenersAreInited = true;
    },

    onSelect: function (selectionModel, record) {
        var me = this;

        if (!me.issues) {
            me.issues = [];
        }
        Ext.Array.include(me.issues, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.issues.length));
        me.getUncheckAllButton().setDisabled(me.issues.length === 0);
        me.getCheckAllButton().setDisabled(me.issues.length === me.getStore().getCount());
        Ext.resumeLayouts(true);
    },

    onBeforeDeselect: function (selectionModel, record) {
        var me = this;

        Ext.Array.remove(me.issues, record.get('id'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.issues.length));
        me.getUncheckAllButton().setDisabled(me.issues.length === 0);
        me.getCheckAllButton().enable();
        Ext.resumeLayouts(true);
    },

    onPrefetch: function (store, records) {
        var me = this,
            selectionModel = me.getSelectionModel(),
            toSelect = [];

        if (Ext.isArray(me.issues)) {
            Ext.Array.each(records, function (record) {
                if (Ext.Array.contains(me.issues, record.get('id'))) {
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
        me.getCheckAllButton().enable();
        me.getSelectionCounter().setText(me.counterTextFn(0));

        button.disable();

        Ext.resumeLayouts(true);

        if (me.issues) {
            me.issues = [];
        }
    },

    onClickCheckAllButton: function (button) {
        var me = this,
            collectionOfDevices = me.getStore().data.getArray()[0],
            selectionModel = me.getSelectionModel();

        Ext.suspendLayouts();

        if (collectionOfDevices) {
            for (var index = 0; index < collectionOfDevices.length; index++) {
                selectionModel.doSelect(index, true);
            }
        }

        if (button) {
            button.disable();
        }

        Ext.resumeLayouts(true);
    }

});