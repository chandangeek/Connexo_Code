Ext.define('Uni.property.view.property.RadioGroup', {
    extend: 'Uni.property.view.property.Base',

    labelWidth: 250,
    controlsWidth: 600,

    getEditCmp: function () {
        var me = this,
            items = [];

        Ext.Array.each(me.getProperty().getPossibleValues(), function (item) {
            items.push({
                xtype: 'radiofield',
                boxLabel: item.name,
                name: 'rg',
                inputValue: item.id
            });
        });

        return [
            {
                items: [
                    {
                        xtype: 'radiogroup',
                        labelWidth: 0,
                        width: me.controlsWidth,
                        itemId: 'radiogroup',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        items: items
                    }
                ]
            }
        ]
    },

    getField: function () {
        return this.down('#radiogroup');
    },

    setValue: function (value) {
        this.getField().setValue({rg: value});
    },

    getValue: function () {
        return this.getField().getValue().rg;
    }
});