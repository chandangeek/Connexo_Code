Ext.define('Uni.property.view.property.CodeTable', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return [
            {
                xtype: 'textfield',
                name: this.getName(),
                itemId: me.key + 'codetable',
                width: me.width,
                readOnly: true
            },
            {
                xtype: 'button',
                text: '...',
                scale: 'small',
                action: 'showCodeTable',
                disabled: me.isReadOnly
            }
        ];
    },

    getField: function () {
        return this.down('textfield');
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (enable) {
                this.getField().enable();
                this.down('button').enable();
            } else {
                this.getField().disable();
                this.down('button').disable();
            }
        }
    }
});