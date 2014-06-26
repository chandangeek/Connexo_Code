Ext.define('Uni.property.view.property.Text', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        return {
            xtype: 'textfield',
            name: 'properties.' + me.key,
            itemId: me.key + 'textfield',
            width: me.width,
            msgTarget: 'under'
        }
    },

    getField: function () {
        return this.down('textfield');
    }
});