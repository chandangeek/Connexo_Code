Ext.define('Uni.view.form.field.Vtypes', {

    requires: ['Ext.form.field.VTypes'],

    hexstringRegex: /^[a-f_A-F_0-9]*$/,

    init: function () {
        this.validateHexString();
        this.validateEan13String();
        this.validateEan18String();
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
    },

    validateEan13String: function () {
        var me = this;

        Ext.apply(Ext.form.field.VTypes, {
            ean13: function (val) {
                //check value
                if (val.length != 13) {
                    return false;
                } else if (me.validateNumeric(val) === false) {
                    return false;
                } else if (val.substr(12) !== me.validateCheckDigit(val.substring(0, 12))) {
                    return false;
                } else {
                    return true;
                }
            },
            ean13stringText: 'Wrong Ean13!'
        });
    },

    numericregex: /^[0-9]$/,
    validateEan18String: function () {
        var me = this;
        Ext.apply(Ext.form.field.VTypes, {
            ean18: function (val) {
                //check value
                if (val.length !== 18) {
                    return false;
                } else if (me.validateNumeric(val) === false) {
                    return false;
                } else if (val.substr(17) !== me.validateCheckDigit(val.substring(0, 17))) {
                    return false;
                } else {
                    return true;
                }
            },
            ean18stringText: 'Wrong Ean18!'
        });
    },

    validateNumeric: function (value) {
        return this.numericregex.test(value);
    },

    validateCheckDigit: function (value) {
        var multiplier = 3;
        var sum = 0;

        for (var i = value.length - 1; i >= 0; i--) {
            var digit = value.substring(i, i + 1);
            sum += digit * multiplier;
            multiplier = (multiplier === 3) ? 1 : 3;
        }
        var next10 = (((sum - 1) / 10) + 1) * 10;
        return next10 - sum;
    }


});