Ext.define('Uni.util.I18n', {
    // Silence is golden.
});

function I18n() {
}

Ext.require('Uni.store.Translations', function () {
    I18n.store = Ext.create('Uni.store.Translations');
});

/**
 * Returns the text translation of the key.
 * @param key Translation key to look up.
 * @returns String
 */
I18n.t = function (key) {
    // TODO Use the arguments to fill in the parameters %0, %1, ...
    return this.store.getById(key).data.value;
};