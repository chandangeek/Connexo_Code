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
        return Uni.I18n.translatePlural('general.nrOfRegisterTypes.selected', count, 'MDC',
            'No register types selected', '{0} register type selected', '{0} register types selected');
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
            xtype: 'reading-type-column',
            dataIndex: 'readingType',
            flex: 2
        },
        {
            xtype: 'obis-column',
            dataIndex: 'obisCode',
            flex: 1
        }
    ],


    onClickUncheckAllButton: function (button) {
        var me = this;
        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
    },

    onSelectionChange: function () {
        var me = this, selection = me.getSelectionModel().getSelection();
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        me.doLayout();
        Ext.resumeLayouts(true);
    }
});