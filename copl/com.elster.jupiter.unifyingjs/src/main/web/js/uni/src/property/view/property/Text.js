Ext.define('Uni.property.view.property.Text', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        return {
            xtype: 'textfield',
            name: this.getName(),
            itemId: me.key,
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank,
            inputType: me.inputType,
            blankText: me.blankText
        }
    },

    getField: function () {
        return this.down('textfield');
    }
});