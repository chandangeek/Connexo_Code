/**
 * @class Uni.I18n
 *
 * Internationalization (I18N) class that can be used to retrieve translations from the translations
 * REST interface. It uses the {@link Ldr.store.Translations} store to retrieve all the available
 * translations for certain components when loading an application.
 *
 * # How to initialize the component translations
 *
 * You need to initialize what translation components should be loaded before you start up the
 * application. Otherwise your translations will not be available. This can be done before calling
 * {@link Uni.Loader#onReady} with the {@link Uni.Loader#initI18n} function. Be sure to include
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
 * # General notation
 *
 * In order to use the internationalization object you need to call the  fully qualified name of
 * {@link Uni.I18n}, as shown below.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: Uni.I18n.translate('my.key', 'UNI', 'Fallback')
 *     });
 *
 * Be mindful that you will need to add {@link Uni.I18n} as a requirement every time, which can be
 * easily forgotten for something as frequently used as internationalization.
 *
 * # Translating simple string values
 *
 * For simple translations you can directly ask the {@link #translate} function to return the translation
 * for a component. Optionally, yet recommended, is to add a fallback translation in case no translation
 * is found.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: Uni.I18n.translate('my.key', 'UNI', 'Fallback')
 *     });
 *
 * More information and examples can be found at the {@link #translate} function.
 *
 * # Translating plural string values
 *
 * There is built-in support for having simple plural versions of a string translation. Summarized, this
 * means that you can have compounded keys that mention which translation should be used for what
 * specific amount.
 *
 * A use case would be having a separate translation for 'no items', '1 item' and '2 items'.
 *
 * More information and examples can be found at the {@link #translatePlural} function.
 *
 * # Formatting dates
 *
 * When you want to format dates a similar template applies as with string formatting. You need a key,
 * a value to format, a component for which it applies, and a fallback format. The date format needs
 * to conform to the Moment.js library.
 *
 * A full list of supported formats can be found at the {Ext.Date} documentation.
 *
 *     @example
 *     var formattedNow = Uni.I18n.formatDate('long.date.format', new Date(), 'UNI', ''F j Y g:i A'');
 *     console.log(formattedNow); // January 28 2014 11:14 AM
 *
 * More information and examples can be found at the {@link #formatDate} function.
 *
 * # Formatting numbers
 *
 * To format numbers in a simple way, there is the {@link #formatNumber} function which requires
 * only a few parameters to work. First of all, a number to format, and secondly, the component for
 * which to format the number. There's also an optional parameter for the number of decimals that
 * should be used.
 *
 *     @example
 *     var formattedNumber = Uni.I18n.formatNumber(130000.037, 'UNI');
 *     console.log(formattedNumber); // 130,000.04
 *
 * More information and examples can be found at the {@link #formatNumber} function.
 *
 * # Formatting currency
 *
 * Currency formatting relates to number formatting, in a way that the number representing is
 * formatted first by the {@link #formatNumber} function. That formatted number is then used to
 * create a complete formatted currency string.
 *
 *     @example
 *     var formattedCurrency = Uni.I18n.formatCurrency(130000.037, 'UNI');
 *     console.log(formattedCurrency); // €130,000.04
 *
 * More information and examples can be found at the {@link #formatCurrency} function.
 *
 */
Ext.define('Uni.I18n', {
    singleton: true,

    requires: [
        'Ldr.store.Translations',
        'Uni.util.String',
        'Uni.DateTime',
        'Uni.Number'
    ],

    /**
     * Default currency format key to perform translation look-ups with.
     *
     * @property {String} [currencyFormatKey='currencyFormat']
     */
    currencyFormatKey: 'currencyFormat',
    /**
     * Default decimal separator format key to perform translation look-ups with.
     *
     * @property {String} [decimalSeparatorKey='decimalSeparator']
     */
    decimalSeparatorKey: 'format.number.decimalseparator',
    /**
     * Default thousands separator format key to perform translation look-ups with.
     *
     * @property {String} [thousandsSeparatorKey='thousandsSeparator']
     */
    thousandsSeparatorKey: 'format.number.thousandsseparator',

    //<debug>
    // Used to only show missing translation messages once.
    blacklist: [],
    //</debug>

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    init: function (components) {
        // TODO Refactor.
        Ldr.store.Translations.setComponents(components);
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
            index = Ldr.store.Translations.findBy(function (record) {
                return record.data.key === key && record.data.cmp === component;
            });
            translation = Ldr.store.Translations.getAt(index);
        } else {
            translation = Ldr.store.Translations.getById(key);
        }

        if (typeof translation !== 'undefined' && translation !== null) {
            translation = translation.data.value;
        } else {
            //<debug>
            if (!this.blacklist[key + component]) {
                this.blacklist[key + component] = true;
                var warning = 'Missing translation for key \'' + key + '\'';
                if (component) {
                    warning += ' in component \'' + component + '\'.';
                } else {
                    warning += '.';
                }
                console.warn(warning);
            }
            //</debug>
        }

        return translation;
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
    translate: function (key, component, fallback, values, htmlEncode) {
        htmlEncode = typeof htmlEncode !== 'undefined' ? htmlEncode : true;
        var translation = this.lookupTranslation(key, component);

        if ((typeof translation === 'undefined' || translation === null)
            && typeof fallback === 'undefined' && fallback === null) {
            translation = key;
        }

        if ((typeof translation === 'undefined' || translation === null)
            && typeof fallback !== 'undefined' && fallback !== null) {
            translation = fallback;
        }

        if (typeof translation !== 'undefined' && translation !== null
            && typeof values !== 'undefined') {
            for (var i = 0; i < values.length; i++) {
                if(htmlEncode === true){
                    var value = Ext.String.htmlEncode(values[i]);
                } else {
                    var value = values[i];
                }
                translation = Uni.util.String.replaceAll(translation, i, value);
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
            translation = Uni.util.String.replaceAll(translation, 0, amount);
        }

        return translation;
    },

    /**
     * @deprecated Use {Uni.DateTime) instead!
     *
     * Formats a date based on a translation key. If no date has been given, the current date is used.
     *
     * The used parse syntax is that of ExtJS which can be found at the {Ext.Date} documentation.
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

        return Ext.Date.format(date, format);
    },

    /**
     * @deprecated Use {Uni.Number} instead!
     *
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
        var decimalSeparator = this.translate(this.decimalSeparatorKey, component, '.'),
            thousandsSeparator = this.translate(this.thousandsSeparatorKey, component, ',');

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

        return this.translate(this.currencyFormatKey, component, formattedValue, [formattedValue]);
    }

});