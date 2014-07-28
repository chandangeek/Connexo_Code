Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileTypeAddMeasurementTypesDockedItems',
    align: 'left',

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'measurementTypesCountContainer',
                margin: '0 8 0 0'
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.uncheckall', 'MDC', 'Uncheck all'),
                action: 'uncheckallmeasurementtypes',
                itemId: 'uncheckAllMeasurementTypes'
            }
        )
    }
});