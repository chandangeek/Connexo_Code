Ext.define('Uni.property.view.property.Password', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.form.field.Password'
    ],

    getEditCmp: function () {
        var me = this;
        return {
            xtype: 'password-field',
            name: this.getName(),
            itemId: me.key + 'passwordfield',
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            fieldLabel: undefined
        }
    },

    getField: function () {
        return this.down('password-field');
    },

    getPasswordField: function () {
        return this.down('textfield');
    },


    initListeners: function () {
        var me = this;
        this.callParent(arguments);
        var field = me.getPasswordField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
    }
});