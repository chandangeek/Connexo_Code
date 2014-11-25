Ext.define('Cfg.view.validation.AddReadingTypesBulk', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.addReadingTypesBulk',
    store: 'AdaptedReadingTypes',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'validation.readingTypes.counterText',
            count,
            'CFG',
            '{0} reading types selected'
        );
    },

    bottomToolbarHidden: true,

    allLabel: Uni.I18n.translate('validation.allReadingTypes', 'CFG', 'All reading types'),
    allDescription: Uni.I18n.translate(
        'general.selectAllItemsRelatedToAppliedFilter',
        'MDC',
        'Select all items related to applied filters'
    ),

    selectedLabel: Uni.I18n.translate('validation.selectedReadingTypes', 'CFG', 'Selected reading types'),
    selectedDescription: Uni.I18n.translate(
        'general.selectItemsInTable',
        'MDC',
        'Select items in table'
    ),

    columns: [
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 1
        }
    ],

    onBulkSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.up('#addReadingTypesToRuleSetup').down('#buttonsContainer button[name=add]').setDisabled(!me.isAllSelected() && selection.length === 0);
    },

    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this;
        if (me.getView()) {
            var selection = me.getView().getSelectionModel().getSelection();

            me.up('#addReadingTypesToRuleSetup').down('#buttonsContainer button[name=add]').setDisabled(!me.isAllSelected() && selection.length === 0);
            if (!me.isAllSelected()) {
                me.setGridVisible(true);
                me.getView().getSelectionModel().deselectAll();
                me.getView().getSelectionModel().select(0);
            } else {
                me.setGridVisible(false);
            }


        }
    }


});