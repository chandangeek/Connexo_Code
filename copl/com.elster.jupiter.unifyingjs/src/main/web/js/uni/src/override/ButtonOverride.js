/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.ButtonOverride
 */
Ext.define('Uni.override.ButtonOverride', {
    override: 'Ext.button.Button',

    /**
     * Changes the default value from '_blank' to '_self'.
     */
    hrefTarget: '_self'

});