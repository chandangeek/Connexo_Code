Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.load-profile-type-add-register-types-grid',
    store: 'Mdc.store.RegisterTypesToAdd',

    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType',
        'Ext.grid.plugin.BufferedRenderer'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.counterText',
            count,
            'MDC',
            '{0} register types selected'
        );
    },

    allLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.allLabel', 'MDC', 'All register types'),
    allDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.allDescription',
        'MDC',
        'Select all items (related to filters on previous screen)'
    ),

    selectedLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.selectedLabel', 'MDC', 'Selected register types'),
    selectedDescription: Uni.I18n.translate(
        'setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid.selectedDescription',
        'MDC',
        'Select items in table'
    ),

    columns: [
        {
            xtype: 'obis-column',
            dataIndex: 'obisCode',
            flex: 1
        },
        {
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 2
        }
    ],

    existingRecords: [],

    onClickUncheckAllButton: function (button) {
        var me = this;

        me.existingRecords = [];
        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
    },

    onSelectionChange: function () {
        var me = this,
            selection = me.existingRecords;

        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        me.doLayout();
        Ext.resumeLayouts(true);
    }
});