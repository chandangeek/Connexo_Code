/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.form.field.Vtypes
 */
Ext.define('Uni.view.form.field.Vtypes', {

    requires: ['Ext.form.field.VTypes'],

    hexstringRegex: /^[a-f_A-F_0-9]*$/,

    init: function () {
        this.validateNonEmptyString();
        this.validateHexString();
        this.validateEan13String();
        this.validateEan18String();
        this.validateReadingtype();
        this.validateImageFileExtension();
        this.validateFirmwareFileExtension();
        this.validateDoubleExtension();
        this.validateCertificateFile();
        this.validateImportFileExtension();
        this.validateForBlacklistCharacters();
    },

    validateCertificateFile: function () {
        var me = this;
        Ext.apply(Ext.form.VTypes, {
            certificateFileUpload: function (val, field) {
                var fileName = /^.*\.(pem|cert)$/i;
                return fileName.test(val) && me.checkLength(val);
            },
            certificateFileUploadText: Uni.I18n.translate('general.certficateValidationFailed.msg', 'UNI', 'File must be "pem" or "cert" format, with a single extension')
        });
    },

    validateDoubleExtension: function () {
        var me = this;
        Ext.apply(Ext.form.VTypes, {
            fileUpload: function (val, field) {
                return me.checkLength(val);
            },
            fileUploadText: Uni.I18n.translate('general.doubleExtensionValidationFailed.msg', 'UNI', 'File name should contain one and only one extension.')
        });
    },

    validateImportFileExtension: function () {
        var me = this;
        Ext.apply(Ext.form.VTypes, {
            importFileUpload: function (val, field) {
                var fileName = /^.*\.(csv|txt|xlsx|zip\.signed|xls|xml|zip)$/i;
                return fileName.test(val) && me.checkLength(val);
            },
            importFileUploadText: Uni.I18n.translate('general.importValidationFailed.msg', 'UNI', 'File must be "csv", "txt", "xlsx" "xls", "xml", or "zip" format, with a single extension')
        });
    },

    validateFirmwareFileExtension: function () {
        var me = this;
        Ext.apply(Ext.form.VTypes, {
            firmwareFileUpload: function (val, field) {
                var fileName = /^.*\.(bin|dat)$/i;
                return fileName.test(val) && me.checkLength(val);
            },
            firmwareFileUploadText: Uni.I18n.translate('general.firmwareValidationFailed.msg', 'UNI', 'File must be "bin" or "dat" format, with a single extension')
        });
    },

    validateForBlacklistCharacters: function () {
        var me = this;
        Ext.apply(Ext.form.VTypes, {
            checkForBlacklistCharacters: function (value, field) {
                return !(/\<(.*?)\>/.test(value)) && !(/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value));
            },
            checkForBlacklistCharactersText: Uni.I18n.translate('general.htmltag.msg', 'UNI', 'Invalid characters')
        });
    },

    validateImageFileExtension: function () {
        var me = this;
        Ext.apply(Ext.form.field.VTypes, {
            image: function (v) {
                return /^.*\.(jpg|JPG|png|PNG)$/.test(v);
            },
            imageText: Uni.I18n.translate('validation.invalidFileFormat', 'UNI', 'Invalid file format')
        });
    },


    validateReadingtype: function () {
        var me = this;
        var message = null;
        Ext.apply(Ext.form.field.VTypes, {
            readingtype: function (v) {
                return /^\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+$/.test(v);
            },
            readingtypeText: 'Invalid reading type syntax',
            readingtypeMask: /[\d\.]/i
        });
    },


    validateNonEmptyString: function () {
        var me = this;
        var message = null;
        Ext.apply(Ext.form.field.VTypes, {
            nonemptystring: function (val) {
                message = null;
                //check value
                if ((val == null || val == undefined || val == '')) {
                    return false;
                }
                if (val.trim().length == 0) {
                    return false;
                }
            },
            nonemptystringText: 'This field is required'
        });
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
    },

    checkLength: function (val) {
        var tokens = val.split(".");
        if (tokens.length == 2) {
            return true
        } else if (tokens.length == 3) {
            return tokens[2] === 'signed';
        }
        return false;
    }
});