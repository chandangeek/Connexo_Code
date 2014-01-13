Ext.define('Mdc.lib.form.field.Vtypes', {

    hexstringRegex: /^[a-f_A-F_0-9]*$/,

    init: function () {
        var me = this;
        this.validateHexString();
    },
    validateHexString: function () {
        var me = this;

        Ext.apply(Ext.form.field.VTypes, {
            hexstring: function (val) {
                //check value
                return me.hexstringRegex.test(val);
            },
            hexstringText: 'Wrong Hexadecimal number!'

        });
    }
});