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

    setValue: function (value) {
        if (!this.isEdit) {
             value = value ? 'Yes' : 'No';
        }
        this.callParent([value]);
    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: me.key + 'displayfield',
            width: me.width,
            msgTarget: 'under'
        }
    },


    getDisplayField: function () {
        return this.down('displayfield');
    }
});