Ext.define('Uni.util.I18n', {
    // Silence is golden.
});

function I18n() {
}

Ext.require('Uni.store.Translations');

/**
 * Uses a regular expression to find and replace all instances of a translation parameter.
 *
 * @param translation Translation to find and replace the index parameters
 * @param searchIndex Index value to replace with the value
 * @param replaceValue Value to replace search results with
 * @returns string  Replaced translation
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
 * @param key Translation key to look up
 * @param values Values to replace in the translation
 * @returns string
 */
I18n.t = function (key, values) {
    var translation = Uni.store.Translations.getById(key).data.value;

    if (translation !== undefined && values !== undefined) {
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
 * @param key Translation key to look up
 * @param number Number to translate with
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

// TODO Support for number formatting.

// TODO Support for currency formatting.

// TODO Support for date formatting.
