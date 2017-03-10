/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.Number
 */
Ext.define('Uni.Number', {
    singleton: true,

    requires: [
        'Uni.util.Preferences',
        'Uni.util.String'
    ],

    decimalPrecisionKey: 'format.number.decimalprecision',
    decimalSeparatorKey: 'format.number.decimalseparator',
    thousandsSeparatorKey: 'format.number.thousandsseparator',

    currencyKey: 'format.number.currency',

    decimalPrecisionDefault: 2,
    decimalSeparatorDefault: '.',
    thousandsSeparatorDefault: ',',

    currencyDefault: '£ {0}',

    // if decimalPrecision is equals -1, then it uses current number decimals count
    doFormatNumber: function (number, decimalPrecision, decimalSeparator, thousandsSeparator) {
        var me = this,
            n = parseFloat(number),
            c = isNaN(decimalPrecision) ? me.decimalPrecisionDefault : Math.abs(decimalPrecision),
            d = decimalSeparator || me.decimalSeparatorDefault,
            t = (typeof thousandsSeparator === 'undefined') ? me.thousandsSeparatorDefault : thousandsSeparator,
            sign = (n < 0) ? '-' : '',
            i = (!isNaN(decimalPrecision) && decimalPrecision === -1 ? parseInt(n = Math.abs(n)) + '' :  parseInt(n = Math.abs(n).toFixed(c)) + '' ),
            j = ((j = i.length) > 3) ? j % 3 : 0;

        return sign + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (!isNaN(decimalPrecision) && decimalPrecision === -1 ? number.split('.')[1] ? d + number.split('.')[1] : '' : (c ? d + Math.abs(n - i).toFixed(c).slice(2) : ''));
    },

    /**
     * Internationalizes a number based on the number of trailing decimals, decimal separator, and thousands
     * separator for the currently active locale.
     *
     * @param {Number} number Number to format
     * @param {Number} [decimalPrecision] Number of required decimal places
     * @param {String} [decimalSeparator] Decimal separator
     * @param {String} [thousandsSeparator] Thousand separator
     * @returns {String} Formatted number
     */
    formatNumber: function (number, decimalPrecision, decimalSeparator, thousandsSeparator) {
        var me = this;

        if (!decimalPrecision && decimalPrecision !== 0) {
            decimalPrecision = Uni.util.Preferences.lookup(me.decimalPrecisionKey, me.decimalPrecisionDefault);
        }
        decimalSeparator = decimalSeparator || Uni.util.Preferences.lookup(me.decimalSeparatorKey, me.decimalSeparatorDefault);
        thousandsSeparator = thousandsSeparator || Uni.util.Preferences.lookup(me.thousandsSeparatorKey, me.thousandsSeparatorDefault);

        return me.doFormatNumber(number, decimalPrecision, decimalSeparator, thousandsSeparator);
    },

    /**
     * Formats a number into a currency based on the parameters, most of which are optional.
     *
     * @param {Number} number Number to format
     * @param {String} [currency] Currency to use
     * @param {Number} [decimalPrecision] Number of required decimal places
     * @param {String} [decimalSeparator] Decimal separator
     * @param {String} [thousandsSeparator] Thousand separator
     * @returns {String} Formatted number
     */
    formatCurrency: function (number, currency, decimalPrecision, decimalSeparator, thousandsSeparator) {
        var me = this,
            result = me.formatNumber(number, decimalPrecision, decimalSeparator, thousandsSeparator);

        currency = currency || Uni.util.Preferences.lookup(me.currencyKey, me.currencyDefault);

        return Uni.util.String.replaceAll(currency, 0, result);
    }
});