/**
 * @class Uni.form.field.Password
 */
Ext.define('Uni.form.field.Password', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'password-field',
    readOnly: false,
    fieldLabel: Uni.I18n.translate('form.password', 'UNI', 'Password'),
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    passwordAsTextComponent: false,

    handler: function (checkbox, checked) {
        var field = this.down('textfield');
        var input = field.getEl().down('input');

        input.dom.type = checked ? 'text' : 'password';
    },

    items: [
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            inputType: 'password',
            name: this.name,
            readOnly: this.readOnly
        }
    ],

    initComponent: function() {
        this.items[0].name = this.name;
        if (!this.passwordAsTextComponent) {
            this.items[1] = (
                {
                    xtype: 'checkbox',
                    boxLabel: Uni.I18n.translate('comServerComPorts.form.showChar', 'UNI', 'Show characters')
                }
            )
            this.items[1].handler = this.handler;
            this.items[1].scope = this;
        }


        this.callParent(arguments);
    },

    setValue: function(value) {
        this.down('textfield').setValue(value);
    }
});