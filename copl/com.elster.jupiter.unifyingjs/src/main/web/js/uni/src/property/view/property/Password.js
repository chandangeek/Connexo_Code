Ext.define('Uni.property.view.property.Password', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.form.field.Password',
        'Uni.form.field.PasswordDisplay'
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
            fieldLabel: undefined,
            passwordAsTextComponent: me.passwordAsTextComponent
        }
    },

    /**
     * returns basic display field configuration
     * override this method if you want custom look of display field of custom property.
     *
     * @returns {Object}
     */
    getDisplayCmp: function () {
        return {
            xtype: 'password-display-field',
            name: this.getName(),
            itemId: this.key + 'passworddisplayfield'
        }
    },


    getField: function () {
        return this.down('password-field');
    },

    getPasswordField: function () {
        return this.down('textfield');
    },

    getPasswordDisplayField: function () {
        return this.down('password-display-field');
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
    },

    setValue: function (value) {
        if (this.isEdit) {
            if (this.getProperty().get('hasValue')) {
                this.getPasswordField().emptyText = Uni.I18n.translate('Uni.value.provided', 'UNI', 'Value provided - no rights to see the value.');
            } else {
                this.getPasswordField().emptyText = '';
            }
            this.getField().setValue(value);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(value);
            }
        }
    }

})
;