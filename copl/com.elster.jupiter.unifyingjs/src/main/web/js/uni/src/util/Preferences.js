/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.util.Preferences
 *
 */
Ext.define('Uni.util.Preferences', {
    singleton: true,

    requires: [
        'Ldr.store.Preferences'
    ],

    //<debug>
    // Used to only show missing preferences messages once.
    blacklist: [],
    //</debug>

    doLookup: function (key) {
        var me = this,
            preference = Ldr.store.Preferences.getById(key);

        if (typeof preference !== 'undefined' && preference !== null) {
            preference = preference.data.value;
        } else {
            //<debug>
            if (!me.blacklist[key]) {
                me.blacklist[key] = true;
                console.warn('Missing preference for key \'' + key + '\'.');
            }
            //</debug>
        }

        return preference;
    },

    /**
     * Looks up a preference based on a key.
     *
     * @param key
     * @param fallback
     * @returns {String/Number}
     */
    lookup: function (key, fallback) {
        var preference = this.doLookup(key);

        if ((typeof preference === 'undefined' || preference === null)
            && typeof fallback === 'undefined' && fallback === null) {
            preference = key;
        }

        if ((typeof preference === 'undefined' || preference === null)
            && typeof fallback !== 'undefined' && fallback !== null) {
            preference = fallback;
        }

        return preference;
    }
});