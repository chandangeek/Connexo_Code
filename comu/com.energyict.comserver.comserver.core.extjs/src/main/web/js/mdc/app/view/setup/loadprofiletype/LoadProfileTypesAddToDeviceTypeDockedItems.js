Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileTypesAddToDeviceTypeDockedItems',
    aling: 'left',

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'addLoadProfileTypesToDeviceTypeCountContainer'
            },
            {
                xtype: 'button',
                text: 'Uncheck All',
                action: 'uncheckallloadprofiletypes',
                itemId: 'uncheckAllLoadProfileTypes',
                margin: '0 5'
            }
        )
    }
});