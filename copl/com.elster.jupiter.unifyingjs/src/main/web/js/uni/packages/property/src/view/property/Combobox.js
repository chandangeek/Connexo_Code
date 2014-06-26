Ext.define('Uni.property.view.property.Combobox', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: 'properties.' + me.key,
            store: me.getProperty().getPossibleValues(),
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive()
        }
    },

    getField: function () {
        return this.down('combobox');
    }
});