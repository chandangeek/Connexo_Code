Ext.define('Uni.property.view.property.Password', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'checkbox',
            name: 'properties.' + me.key,
            itemId: me.key + 'checkbox',
            width: me.width,
            cls: 'check',
            msgTarget: 'under'
        };
    },

    getField: function () {
        return this.down('checkbox');
    }
});