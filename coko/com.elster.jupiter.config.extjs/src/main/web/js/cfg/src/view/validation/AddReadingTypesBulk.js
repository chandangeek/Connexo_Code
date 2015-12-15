Ext.define('Cfg.view.validation.AddReadingTypesBulk', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.addReadingTypesBulk',
    store: 'Cfg.store.ReadingTypesToAddForRule',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    blockSelectEvent: false,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfReadingTypes.selected', count, 'CFG',
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
        })
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
        button.setDisabled(true);
    },

    overriddenOnSelectionChange: function () {
        var me = this;

        me.getSelectionCounter().setText(me.counterTextFn(me.hiddenSelection.length));
        me.getUncheckAllButton().setDisabled(me.hiddenSelection.length === 0);
        me.up('#addReadingTypesToRuleSetup').down('#buttonsContainer button[name=add]').setDisabled(me.hiddenSelection.length === 0);
    },

    getSelectedRecords: function() {
        return this.hiddenSelection
    },

    clearSelection: function() {
        this.hiddenSelection = [];
    },


    onSelectionChange: function () {
    }
});