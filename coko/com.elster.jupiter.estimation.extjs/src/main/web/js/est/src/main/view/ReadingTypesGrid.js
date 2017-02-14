/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.view.ReadingTypesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.reading-types-grid',
    store: 'Est.main.store.ReadingTypes',

    requires: [
        'Uni.grid.column.ReadingType',
        'Est.main.store.SelectedReadingTypes',
        'Est.main.view.SelectedReadingTypesWindow'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfReadingTypes.selected', count, 'EST',
            'No reading types selected', '{0} reading type selected', '{0} reading types selected'
        );
    },

    bottomToolbarHidden: true,

    columns: [
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;
        me.hiddenSelection = [];
        me.callParent(arguments);

        me.on('select', function (grid, record) {
            if (!me.blockSelectEvent) {
                me.pushRecord(record);
            }
        });
        me.on('deselect', function (grid, record) {
            if (!me.blockSelectEvent) {
                me.popRecord(record);
            }
        });



        me.getStore().on('prefetch', function () {
            me.selectRecords();
        });
        me.getStore().on('beforeload', function () {
            me.deselectGrid();
        });

        me.getTopToolbarContainer().add(1,{
            xtype: 'button',
            hidden: true,
            itemId: 'list-of-reading-types-info-btn',
            tooltip: Uni.I18n.translate('readingType.tooltip', 'EST', 'Click for more information'),
            iconCls: 'uni-icon-info-small',
            cls: 'uni-btn-transparent',
            width: 15,
            style: {
                display: 'inline-block',
                textDecoration: 'none !important',
                position: 'absolute',
                top: '5px'
            },
            handler: function () {
                var widget = Ext.widget('estimationSelectedReadingTypes');
                widget.setTitle(me.counterTextFn(me.hiddenSelection.length));

                widget.show();
            }
        });
    },

    deselectGrid: function() {
        this.blockSelectEvent = true;
        if (this.view) {
            this.view.getSelectionModel().deselectAll();
        }
        this.blockSelectEvent = false;
    },

    popRecord: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('mRID') === record.get('mRID')) {
                index = ind;
            }
        });
        if (index > -1) {
            this.hiddenSelection.splice(index, 1);
        }
        this.overriddenOnSelectionChange();
    },

    pushRecord: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('mRID') === record.get('mRID')) {
                index = ind;
            }
        });
        if (index < 0) {
            this.hiddenSelection.push(record);
        }
        this.overriddenOnSelectionChange();
    },

    selectRecords: function () {
        var me = this,
            recordsArr = [];

        if (!me.blockSelectEvent && me.getSelectionModel().views.length > 0) {
            me.blockSelectEvent = true;
            Ext.defer(function () {
                Ext.each(me.hiddenSelection, function (rec) {
                    var record = me.getStore().findRecord('mRID', rec.get('mRID'));
                    if (record) {
                        recordsArr.push(record);
                    }
                });
                me.getSelectionModel().select(recordsArr);
                me.blockSelectEvent = false;
                me.overriddenOnSelectionChange();
            }, 100);
        }
    },

    onClickUncheckAllButton: function (button) {
        var me = this;
        me.blockSelectEvent = true;
        me.view.getSelectionModel().deselectAll();
        me.clearSelection();
        me.blockSelectEvent = false;
        me.overriddenOnSelectionChange();
        this.down('#list-of-reading-types-info-btn').hide();
        button.setDisabled(true);
    },

    overriddenOnSelectionChange: function () {
        var me = this;

        me.getSelectionCounter().setText(me.counterTextFn(me.hiddenSelection.length));
        me.getUncheckAllButton().setDisabled(me.hiddenSelection.length === 0);
        me.up('#add-reading-types').down('[action=addReadingTypes]').setDisabled(me.hiddenSelection.length === 0);
    },

    getSelectedRecords: function() {
        return this.hiddenSelection
    },

    clearSelection: function() {
        this.hiddenSelection = [];
    },


    onSelectionChange: function () {
        var me =this,
            btn = this.down('#list-of-reading-types-info-btn'),
            selectedStore =  Ext.getStore('Est.main.store.SelectedReadingTypes');
        selectedStore.removeAll();
        selectedStore.add(me.hiddenSelection);
        me.hiddenSelection.length ? btn.show() : btn.hide();
    }
});