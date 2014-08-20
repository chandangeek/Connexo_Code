Ext.define('Uni.property.view.property.Boolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'checkbox',
            name: this.getName(),
            itemId: me.key + 'checkbox',
            width: me.width,
            cls: 'check',
            msgTarget: 'under'
        };
    },

    getField: function () {
        return this.down('checkbox');
    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'checkbox',
            readOnly: true,
            name: this.getName(),
            itemId: me.key + 'checkbox',
            width: me.width,
            cls: 'check',
            msgTarget: 'under'
        };
    },

    getDisplayField: function () {
        return this.down('checkbox');
    }
});