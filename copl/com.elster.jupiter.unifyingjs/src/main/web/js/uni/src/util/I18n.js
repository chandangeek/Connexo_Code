Ext.define('Uni.util.I18n', {
    // Silence is golden.
});

function I18n() {
}

Ext.require('Uni.store.Translations');

/**
 * Initializes the internationalization language that should be used.
 *
 * @param {String} language Language to use
 */
I18n.lang = function (language) {
    // TODO Initialize the language.
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
    var translation = Uni.store.Translations.getById(key);

    if (translation !== undefined && translation !== null) {
        translation = translation.data.value;
    }

    if (values !== undefined) {
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
        translation = Uni.store.Translations.getById(lookup).data.value;

    if (translation === undefined) {
        translation = Uni.store.Translations.getById(key).data.value;
    }

    if (translation !== undefined && number !== undefined) {
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
    date = date ? date : new Date();

    var format = this.t(key, undefined),
        formattedDate = date.toLocaleString();

    if (format !== null) {
        formattedDate = moment(date).format(format);
    }

    return formattedDate;
};

I18n.n = function (key) {
    // TODO Support for number formatting.
};


I18n.c = function (key) {
    // TODO Support for currency formatting.
};