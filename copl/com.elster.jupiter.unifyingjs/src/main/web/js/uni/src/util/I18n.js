/**
 * @class Uni.util.I18n
 */
Ext.define('Uni.util.I18n', {
    requires: ['Uni.store.Translations'],

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    init: function (components) {
        Uni.store.Translations.setComponents(components);
    },

    /**
     * Loads the internationalization translations for the current component settings.
     *
     * @param {Function} [callback] Callback after loading
     */
    load: function (callback) {
        callback = (typeof callback !== 'undefined') ? callback : function () {
        };

        Uni.store.Translations.load({
            callback: function () {
                callback();
            }
        });
    },

    /**
     * Looks up the translation for a certain key. If there is a missing translation, the key
     * will be returned surrounded by square brackets like [this]. In debug there will also
     * be an extra warning that is logged in the debug console.
     *
     * @param {String} key Key to look up the translation for
     * @param {String} component Component to filter on
     * @returns {String} Translation
     */
    lookupTranslation: function (key, component) {
        var translation,
            index;

        if (typeof component !== 'undefined' && component) {
            index = Uni.store.Translations.findBy(function (record) {
                return record.data.key === key && record.data.cmp === component;
            });
            translation = Uni.store.Translations.getAt(index);
        } else {
            translation = Uni.store.Translations.getById(key);
        }

        if (typeof translation !== 'undefined' && translation !== null) {
            translation = translation.data.value;
        } else {
            //<debug>
            var warning = 'Missing translation for key "' + key + '"';
            if (component) {
                warning += ' in component "' + component + '"';
            }
            console.log(warning);
            //</debug>
        }

        return translation;
    },

    /**
     * Uses a regular expression to find and replace all instances of a translation parameter.
     *
     * @param {String} translation Translation to find and replace the index parameters
     * @param {Number} searchIndex Index value to replace with the value
     * @param {String} replaceValue Value to replace search results with
     * @returns {String} Replaced translation
     */
    replaceAll: function (translation, searchIndex, replaceValue) {
        var lookup = '\{[' + searchIndex + ']\}';
        return translation.replace(new RegExp(lookup, 'g'), replaceValue);
    },

    /**
     * Returns the text translation of the key, looking for the key in a certain .
     *
     * @param {String} key Translation key to look up
     * @param {String} component Component on which to filter
     * @param {String} fallback Fallback value in case the tranlation was not found
     * @param {String[]} [values] Values to replace in the translation
     * @returns {String} Translation.
     */
    translate: function (key, component, fallback, values) {
        var translation = this.lookupTranslation(key, component);

        if ((typeof translation === 'undefined' || translation === null)
            && typeof fallback !== 'undefined' && fallback !== null) {
            translation = fallback;
        }

        if (typeof translation !== 'undefined' && translation !== null
            && typeof values !== 'undefined') {
            for (var i = 0; i < values.length; i++) {
                translation = this.replaceAll(translation, i, values[i]);
            }
        }

        return translation;
    },

    /**
     * Looks up the plural translation of a number, e.g. for 0 items the translation could be
     * 'There no items', for 1 item 'There is 1 item', or for 7 items 'There are 7 items'.
     * If your key is named 'itemCount' then for the number 0 will look up 'itemCount[0]',
     * for the number 1 'itemCount[1]', and so on. It falls back on the generic 'itemCount' key.
     *
     * @param {String} key Translation key to look up
     * @param {Number} number Number to translate with
     */
    translatePlural: function (key, number) {
        var lookup = key + '[' + number + ']',
            translation = this.lookupTranslation(lookup);

        if (typeof translation === 'undefined') {
            translation = this.lookupTranslation(key);
        }

        if (typeof number !== 'undefined') {
            translation = this.replaceAll(translation, 0, number);
        }

        return translation;
    },

    /**
     * Formats a date based on a translation key. If no date has been given, the current date is used.
     * The date is formatted based on the browser's locale if no valid parse format has been found.
     * The used parse syntax is that of Moment.js which can be found here:
     * http://www.momentjs.com/docs/#/parsing/string-format/
     *
     * @param {String} key Translation key to format the date with
     * @param {Date} [date] Date to format
     * @returns {String} Formatted date as a string value
     */
    formatDate: function (key, date) {
        // TODO Use a fallback format by loading in languages from Moment.js.
        date = date || new Date();

        var format = this.t(key, undefined),
            formattedDate = date.toLocaleString();

        if (format !== null) {
            formattedDate = moment(date).format(format);
        }

        return formattedDate;
    },

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
    formatNumberWithSeparators: function (number, decimals, decimalSeparator, thousandsSeparator) {
        var n = parseFloat(number),
            c = isNaN(decimals) ? 2 : Math.abs(decimals),
            d = decimalSeparator || '.',
            t = (typeof thousandsSeparator === 'undefined') ? ',' : thousandsSeparator,
            sign = (n < 0) ? '-' : '',
            i = parseInt(n = Math.abs(n).toFixed(c)) + '',
            j = ((j = i.length) > 3) ? j % 3 : 0;

        return sign + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : '');
    },

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
    formatNumber: function (number, decimals) {
        var decimalSeparator = this.translate('decimalSeparator') || '.',
            thousandsSeparator = this.translate('thousandsSeparator') || ',';

        return this.formatNumberWithSeparators(number, decimals, decimalSeparator, thousandsSeparator);
    },

    /**
     * Internationalizes a value into its correct currency format based on the active locale. If the number of
     * trailing decimals is not specified, 2 decimals are used. The lookup key for the currency format
     * is 'currencyFormat'. If the currency format is not found, the formatted numeric value is used.
     *
     * @param {Number} value Currency value to internationalize
     * @param {Number} [decimals] Number of required decimal places
     * @returns {String} Internationalized currency value
     */
    formatCurrency: function (value, decimals) {
        var formattedValue = this.n(value, decimals);

        return this.translate('currencyFormat', undefined, '', [formattedValue]) || formattedValue;
    }

});

var I18n = I18n || Ext.create('Uni.util.I18n');