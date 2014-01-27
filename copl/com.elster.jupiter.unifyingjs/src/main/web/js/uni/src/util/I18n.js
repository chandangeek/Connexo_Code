/**
 * @class Uni.util.I18n
 *
 * Internationalization (I18N) class that can be used to retrieve translations from the translations
 * REST interface. It uses the {@link Uni.store.Translations} store to retrieve all the available
 * translations for certain components when loading an application.
 *
 * # How to initialize the component translations
 *
 * You need to initialize what translation components should be loaded before you start up the
 * application. Otherwise your translations will not be available. This can be done before calling
 * {@link Uni.Loader#onReady} with the {@link Uni.Loader#initI18n} method. Be sure to include
 * an array of component aliases you want to have available in your application.
 *
 *      @example
 *      Ext.require('Uni.Loader');
 *      Ext.onReady(function () {
 *          var loader = Ext.create('Uni.Loader');
 *          loader.initI18n(['MTR', 'USR', 'PRT']); // Component UNI automatically included.
 *
 *          loader.onReady(function () {
 *              // Start up the application.
 *          });
 *      });
 *
 * Note that the UnifyingJS (alias **UNI**) component translations are always loaded in as well.
 * This is to make sure that the components render correctly for all languages.
 *
 * # Short notation
 *
 * There are 2 ways of using the internationalization object, the most pragmatic one being the preferred way.
 * You can just call the global I18n variable that has a reference to a singleton {@link Uni.util.I18n}
 * instance.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: I18n.translate('my.key', 'CMP', 'Fallback')
 *     });
 *
 * The main advantage of this way is that it can be used without having to declare separate variables,
 * take requirements into consideration, and improve code readability. The other way is to call the
 * fully qualified name of {@link Uni.util.I18n} instead, as shown below.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: Uni.util.I18n.translate('my.key', 'CMP', 'Fallback')
 *     });
 *
 * Be mindful that you will need to add {@link Uni.util.I18n} as a requirement every time, which can be
 * easily forgotten for something as frequently used as internationalization.
 *
 * # Translating simple string values
 *
 * For simple translations you can directly ask the {@link #translate} method to return the translation
 * for a component. Optionally, yet recommended, is to add a fallback translation in case no translation
 * is found.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: translate('my.key', 'CMP', 'Fallback')
 *     });
 *
 * More information and examples can be found at the {@link #translate} method.
 *
 * # Translating plural string values
 *
 * There is built-in support for having simple plural versions of a string translation. Summarized, this
 * means that you can have compounded keys that mention which translation should be used for what
 * specific amount.
 *
 * A use case would be having a separate translation for 'no items', '1 item' and '2 items'.
 *
 * More information and examples can be found at the {@link #translatePlural} method.
 *
 * # Formatting dates
 *
 * **Under development**
 *
 * # Formatting numbers
 *
 * **Under development**
 *
 * # Formatting currency
 *
 * **Under development**
 *
 */
Ext.define('Uni.util.I18n', {
    singleton: true,
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
     * @param {String} fallback Fallback value in case the translation was not found
     * @param {String[]} [values] Values to replace in the translation
     * @returns {String} Translation
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
     * If your key is named 'itemCount' then for the amount 0 will look up 'itemCount[0]',
     * for the amount 1 'itemCount[1]', and so on. It falls back on the generic 'itemCount' key.
     *
     * @param {String} key Translation key to look up
     * @param {Number/String} amount Amount to translate with
     * @param {String} component Component to look up the translation for
     * @param {String} fallback Fallback value in case the translation was not found
     */
    translatePlural: function (key, amount, component, fallback) {
        var lookup = key + '[' + amount + ']',
            translation = this.lookupTranslation(lookup, component);

        if (typeof translation === 'undefined') {
            translation = this.lookupTranslation(key, component) || fallback;
        }

        if (typeof amount !== 'undefined') {
            translation = this.replaceAll(translation, 0, amount);
        }

        return translation;
    },

    /**
     * Formats a date based on a translation key. If no date has been given, the current date is used.
     *
     * The used parse syntax is that of Moment.js which can be found here:
     * http://www.momentjs.com/docs/#/parsing/string-format/
     *
     * @param {String} key Translation key to format the date with
     * @param {Date} [date=new Date()] Date to format
     * @param {String} [component] Component to look up the format for
     * @param {String} [fallback] Fallback format
     * @returns {String} Formatted date as a string value
     */
    formatDate: function (key, date, component, fallback) {
        date = date || new Date();

        var format = this.translate(key, component, fallback);

        return moment(date).format(format);
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
     * @param {Number} [decimals=2] Number of required decimal places
     * @param {String} [decimalSeparator=.] Required decimal separator
     * @param {String} [thousandsSeparator=,] Required thousand separator
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
     * @param {Number} number Number to internationalize
     * @param {String} [component] Component to look up the format for
     * @param {Number} [decimals] Number of required decimal places
     * @returns {String} Internationalized number
     */
    formatNumber: function (number, component, decimals) {
        var decimalSeparator = this.translate('decimalSeparator', component, '.'),
            thousandsSeparator = this.translate('thousandsSeparator', component, ',');

        return this.formatNumberWithSeparators(number, decimals, decimalSeparator, thousandsSeparator);
    },

    /**
     * Internationalizes a value into its correct currency format based on the active locale. If the number of
     * trailing decimals is not specified, 2 decimals are used. The lookup key for the currency format
     * is 'currencyFormat'. If the currency format is not found, the formatted numeric value is used.
     *
     * @param {Number} value Currency value to internationalize
     * @param {String} [component] Component to look up the format for
     * @param {Number} [decimals] Number of required decimal places
     * @returns {String} Internationalized currency value
     */
    formatCurrency: function (value, component, decimals) {
        var formattedValue = this.formatNumber(value, component, decimals);

        return this.translate('currencyFormat', component, formattedValue, [formattedValue]);
    }

});