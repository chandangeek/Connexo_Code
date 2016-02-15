Ext.define('Imt.usagepointmanagement.view.landingpageattributes.QuantityAttribute', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.quantity-edit-field',



    //layout: {
    //    type: 'hbox',
    //    //align: 'stretch'
    //},

    initComponent: function () {
        var me = this;
        me.items = [
            {
                //itemId: 'quantity-value-numberfield',
                xtype: 'numberfield',
                labelWidth: 250,
                fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage'),
                width: 350,
            },
            {
                width: 80,
                labelWidth: 350,
                margin: '0 0 0 10',
                xtype:'combobox',
                //name: 'phaseCode',
                //itemId: 'up-phase-textfield',
                //fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code')

            }
        ];
        me.callParent();
    },

    setQuantityValue: function(data){
        var me = this;
        data.value ? me.down('numberfield').setValue(data.value) : me.down('numberfield').setValue(0);
        data.unit ? me.down('combobox').setValue(data.unit) : me.down('combobox').setValue('');
    }
});