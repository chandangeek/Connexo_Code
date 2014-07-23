Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileTypesAddToDeviceTypeDockedItems',
    align: 'left',

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'addLoadProfileTypesToDeviceTypeCountContainer'
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.uncheckall', 'MDC', 'Uncheck all'),
                action: 'uncheckallloadprofiletypes',
                itemId: 'uncheckAllLoadProfileTypes',
                margin: '0 0 0 8'
            },
            {
                xtype: 'container',
                flex: 1
            },
            {
                xtype: 'button',
                ui: 'link',
                text: Uni.I18n.translate('loadprofiletypes.manageloadprofiletypes', 'MDC', 'Manage load profile types'),
                handler: function (button, event) {
                    window.open('#/administration/loadprofiletypes');
                }
            }
        )
    }
});