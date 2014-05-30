Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileTypeAddMeasurementTypesDockedItems',
    aling: 'left',

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'measurementTypesCountContainer'
            },
            {
                xtype: 'button',
                text: 'Uncheck All',
                action: 'uncheckallmeasurementtypes',
                itemId: 'uncheckAllMeasurementTypes',
                ui: 'action'
            }
        )
    }
});