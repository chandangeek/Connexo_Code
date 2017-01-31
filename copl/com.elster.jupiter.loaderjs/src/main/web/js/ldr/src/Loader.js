/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ldr.Loader
 *
 * Class responsible for loading in an application after the privileges and
 * translations have finished loading.
 */
Ext.define('Ldr.Loader', {
    appScript: 'app.js',

    requires: [
        'Ldr.store.Preferences',
        'Ldr.store.Privileges',
        'Ldr.store.Translations',
        'Ldr.store.Pluggable'
    ],

    /**
     * Used to count if both preferences and translations have been loaded.
     */
    loadCallbackCounter: 0,

    /**
     * Called whenever Ext has finished loading. Loads the privileges and then
     * requests the translations.
     */
    onReady: function () {
        var scope = this;

        Ext.Loader.setConfig({
            enabled: true,
            disableCaching: false
        });

        scope.loadPrivileges(scope.onPrivilegesLoad, scope);
    },

    /**
     * Loads up the privileges and then the translations.
     *
     * @param {Object} scope Scope
     */
    onPrivilegesLoad: function (scope) {
        scope.loadCallbackCounter = 0;

        scope.loadPreferences(scope.checkAppLoadable, scope);
        scope.loadTranslations(scope.checkAppLoadable, scope);

        // TODO Enable again whenever pluggable classes are supported again at the back-end.
        //scope.loadPlugins(scope.checkAppLoadable, scope);
    },

    /**
     * Loads up the application script after the translations have been loaded.
     *
     * @param {Object} scope Scope
     */
    checkAppLoadable: function (scope) {
        if (scope.loadCallbackCounter > 1 && !scope.isAppScriptLoaded(scope)) {
            scope.loadApp(scope);
        } else if (scope.loadCallbackCounter > 1 && scope.isAppScriptLoaded(scope)) {
            Ldr.store.Pluggable.each(function (pluginScript) {
                Ext.Loader.setPath(pluginScript.get('name'), pluginScript.get('basePath') + '/src');
                _.each(pluginScript.get('scripts'),function(script){
                    Ext.Loader.loadScript({
                        url: pluginScript.get('basePath') + script.path + '/' + script.name,
                        onError: function () {
                            console.error('*** Could not load a plugin script!');

                            // TODO Redirect to an error page.
                        }
                    });
                })
            });
        }
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
     * Loads the preferences for the current user.
     *
     * @param {Function} callback Callback after loading
     * @param {Object} scope Scope
     */
    loadPreferences: function (callback, scope) {
        Ldr.store.Preferences.load({
            callback: function (records, operation, success) {
                scope.loadCallbackCounter++;

                //<debug>
                if (!success) {
                    console.warn('Preferences could not be loaded, continuing anyways.');
                }
                //</debug>

                callback(scope);
            }
        });
    },

    loadPlugins: function (callback, scope) {
        Ldr.store.Pluggable.load({
            callback: function (records, operation, success) {
                scope.loadCallbackCounter++;
                //<debug>
                if (!success) {
                    console.warn('Plugins could not be loaded, continuing anyways.');
                }
                //</debug>

                callback(scope);
            }
        })
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
                scope.loadCallbackCounter++;

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
        var me = this;
        Ext.Loader.loadScript({
            url: scope.appScript,
            onError: function () {
                console.error('Could not load the application!');

                // TODO Redirect to an error page.
            },
            onLoad: function () {
                Ldr.store.Pluggable.each(function (pluginScript) {
                    Ext.Loader.setPath(pluginScript.get('name'), pluginScript.get('basePath') + '/src');
                    _.each(pluginScript.get('scripts'),function(script){
                        Ext.Loader.loadScript({
                            url: pluginScript.get('basePath') + script.path + '/' + script.name,
                            onError: function () {
                                console.error('*** Could not load a plugin script!');

                                // TODO Redirect to an error page.
                            }
                        });
                    })
                });
            }
        });
    },

    /**
     * Checks whether the app script has already been loaded.
     *
     * @param scope
     * @returns {boolean}
     */
    isAppScriptLoaded: function (scope) {
        var scripts = document.scripts;

        for (var i = 0; i < scripts.length; i++) {
            var script = scripts[i];

            if (script.src.indexOf(scope.appScript) > 0) {
                return true;
            }
        }

        return false;
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