/**
 * @class Ldr.Loader
 *
 * Class responsible for loading in an application after the privileges and
 * translations have finished loading.
 */
Ext.define('Ldr.Loader', {
    appScript: 'app.js',

    requires: [
        'Ldr.store.Privileges',
        'Ldr.store.Translations'
    ],

    /**
     * Called whenever Ext has finished loading. Loads the privileges and then
     * requests the translations.
     */
    onReady: function () {
        var scope = this;

        scope.loadPrivileges(scope.onPrivilegesLoad, scope);
    },

    /**
     * Loads up the privileges and then the translations.
     *
     * @param {Object} scope Scope
     */
    onPrivilegesLoad: function (scope) {
        scope.loadTranslations(scope.onTranslationsLoad, scope);
    },

    /**
     * Loads up the application script after the translations have been loaded.
     *
     * @param {Object} scope Scope
     */
    onTranslationsLoad: function (scope) {
        scope.loadApp(scope);
    },

    /**
     * Loads the privileges for the current user.
     *
     * @param {Function} callback Callback after loading
     * @param {Object} scope Scope
     */
    loadPrivileges: function (callback, scope) {
        callback = (typeof callback !== 'undefined') ? callback : function () {
        };

        Ldr.store.Privileges.load({
            callback: function (records, operation, success) {
                if (success) {
                    callback(scope);
                } else {
                    //<debug>
                    console.error('Privileges could not be loaded, loading aborted.');
                    //</debug>

                    // TODO Redirect to the login app with a specific warning.
                }
            }
        });
    },

    /**
     * Loads the internationalization translations for the current component settings.
     * The array 'i18nComponents' should be defined in the index file for the translations.
     *
     * @param {Function} callback Callback after loading
     * @param {Object} scope Scope
     */
    loadTranslations: function (callback, scope) {
        callback = (typeof callback !== 'undefined') ? callback : function () {
        };

        // Should be defined as a script tag is the index file.
        var i18nComponents = window.i18nComponents || [];
        Ldr.store.Translations.setComponents(i18nComponents);

        Ldr.store.Translations.load({
            callback: function (records, operation, success) {
                //<debug>
                if (!success) {
                    console.warn('Translations could not be loaded, continuing anyways.');
                }
                //</debug>

                callback(scope);
            }
        });
    },

    /**
     * Loads up the application script.
     *
     * @param {Object} scope Scope
     */
    loadApp: function (scope) {
        Ext.Loader.loadScript({
            url: scope.appScript,
            onError: function () {
                console.error('Could not load the application!');

                // TODO Redirect to an error page.
            }
        });
    }
});

/**
 * Loads up the loader functionality.
 */
Ext.onReady(function () {
    Ext.require('Ldr.Loader', function () {
        var loader = Ext.create('Ldr.Loader');
        loader.onReady();
    });
});