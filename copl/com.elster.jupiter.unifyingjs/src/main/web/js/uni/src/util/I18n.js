Ext.define('Uni.util.I18n', {
    // Silence is golden.
});

function I18n() {
}

Ext.require('Uni.store.Translations');

/**
 * Loads the internationalization locale that should be used.
 *
 * @param {String} locale Locale to use
 * @param {String} component Component to load
 * @param {Function} [callback] Callback after loading
 */
I18n.loadLocale = function (locale, component, callback) {
    locale = (typeof locale !== 'undefined') ? locale : 'en-GB';
    Uni.store.Translations.setLocale(locale);

    component = (typeof component !== 'undefined') ? component : 'all';
    Uni.store.Translations.setComponent(component);

    callback = (typeof callback !== 'undefined') ? callback : function () {
    };

    Uni.store.Translations.load({
        callback: callback
    });
};

/**
 * Looks up the translation for a certain key. If there is a missing translation, the key
 * will be returned surrounded by square brackets like [this]. In debug there will also
 * be an extra warning that is logged in the debug console.
 *
 * @param {String} key Key to look up the translation for
 * @returns {String} Translation
 */
I18n.lookupTranslation = function (key) {
    var translation = Uni.store.Translations.getById(key);

    if (typeof translation !== 'undefined' && translation !== null) {
        translation = translation.data.value;
    } else {
        translation = '[' + key + ']';
        //<debug>
        console.warn('Missing translation for key: ' + key);
        //</debug>
    }

    return translation;
};

/**
 * Uses a regular expression to find and replace all instances of a translation parameter.
 *
 * @param {String} translation Translation to find and replace the index parameters
 * @param {Number} searchIndex Index value to replace with the value
 * @param {String} replaceValue Value to replace search results with
 * @returns {String} Replaced translation
 */
I18n.replaceAll = function (translation, searchIndex, replaceValue) {
    var lookup = '\{[' + searchIndex + ']\}';
    return translation.replace(new RegExp(lookup, 'g'), replaceValue);
};

/**
 * Returns the text translation of the key.
 *
 * The 't' short notation stands for 'translate'.
 *
 * @param {String} key Translation key to look up
 * @param {String[]} [values] Values to replace in the translation
 * @returns {String} Translation.
 */
I18n.t = function (key, values) {
    var translation = I18n.lookupTranslation(key);

    if (typeof translation !== 'undefined' && translation !== null && typeof values !== 'undefined') {
        for (var i = 0; i < values.length; i++) {
            translation = I18n.replaceAll(translation, i, values[i]);
        }
    }

    return translation;
};

/**
 * Looks up the plural translation of a number, e.g. for 0 items the translation could be
 * 'There no items', for 1 item 'There is 1 item', or for 7 items 'There are 7 items'.
 * If your key is named 'itemCount' then for the number 0 will look up 'itemCount[0]',
 * for the number 1 'itemCount[1]', and so on. It falls back on the generic 'itemCount' key.
 *
 * The 'p' short notation stands for 'plural'.
 *
 * @param {String} key Translation key to look up
 * @param {Number} number Number to translate with
 */
I18n.p = function (key, number) {
    var lookup = key + '[' + number + ']',
        translation = I18n.lookupTranslation(lookup);

    if (typeof translation === 'undefined') {
        translation = I18n.lookupTranslation(key);
    }

    if (typeof number !== 'undefined') {
        translation = I18n.replaceAll(translation, 0, number);
    }

    return translation;
};

/**
 * Formats a date based on a translation key. If no date has been given, the current date is used.
 * The date is formatted based on the browser's locale if no valid parse format has been found.
 * The used parse syntax is that of Moment.js which can be found here:
 * http://www.momentjs.com/docs/#/parsing/string-format/
 *
 * The 'd' short notation stands for 'date'.
 *
 * @param {String} key Translation key to format the date with
 * @param {Date} [date] Date to format
 * @returns {String} Formatted date as a string value
 */
I18n.d = function (key, date) {
    // TODO Use a fallback format by loading in languages from Moment.js.
    date = date || new Date();

    var format = this.t(key, undefined),
        formattedDate = date.toLocaleString();

    if (format !== null) {
        formattedDate = moment(date).format(format);
    }

    return formattedDate;
};

/**
 * Formats a number based on parameters for the number of trailing decimal places, what decimal
 * separator should be used, and what the thousands separator is. If the number of trailing
 * decimals is not specified, 2 decimals are used. By default the decimal separator is '.' and
 * the thousands separator is ','.
 *
 * Adapted from: http://stackoverflow.com/a/149099/682311
 *
 * @param {Number} number Number to format
 * @param {Number} [decimals] Number of required decimal places
 * @param {String} [decimalSeparator] Required decimal separator
 * @param {String} [thousandsSeparator] Required thousand separator
 * @returns {String} Formatted number
 */
I18n.formatNumber = function (number, decimals, decimalSeparator, thousandsSeparator) {
    var n = parseFloat(number),
        c = isNaN(decimals) ? 2 : Math.abs(decimals),
        d = decimalSeparator || '.',
        t = (typeof thousandsSeparator === 'undefined') ? ',' : thousandsSeparator,
        sign = (n < 0) ? '-' : '',
        i = parseInt(n = Math.abs(n).toFixed(c)) + '',
        j = ((j = i.length) > 3) ? j % 3 : 0;

    return sign + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : '');
};

/**
 * Internationalizes a number based on the number of trailing decimals, decimal separator, and thousands
 * separator for the currently active locale. If the number of trailing decimals is not specified,
 * 2 decimals are used. The translation lookup key for the decimal separator is 'decimalSeparator,
 * while the thousands separator has the lookup key 'thousandsSeparator'.
 *
 * The 'n' short notation stands for 'number'.
 *
 * @param {Number} number Number to internationalize
 * @param {Number} [decimals] Number of required decimal places
 * @returns {String} Internationalized number
 */
I18n.n = function (number, decimals) {
    var decimalSeparator = this.t('decimalSeparator'),
        thousandsSeparator = this.t('thousandsSeparator');

    return this.formatNumber(number, decimals, decimalSeparator, thousandsSeparator);
};

/**
 * Internationalizes a value into its correct currency format based on the active locale. If the number of
 * trailing decimals is not specified, 2 decimals are used. The lookup key for the currency format
 * is 'currencyFormat'. If the currency format is not found, the formatted numeric value is used.
 *
 * The 'c' short notation stands for 'currency'.
 *
 * @param {Number} value Currency value to internationalize
 * @param {Number} [decimals] Number of required decimal places
 * @returns {String} Internationalized currency value
 */
I18n.c = function (value, decimals) {
    var formattedValue = this.n(value, decimals);

    return this.t('currencyFormat', [formattedValue]) || formattedValue;
};