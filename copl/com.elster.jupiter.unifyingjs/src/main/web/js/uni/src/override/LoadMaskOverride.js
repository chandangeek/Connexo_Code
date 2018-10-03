/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define("Uni.override.LoadMaskOverride", {
    override: "Ext.LoadMask",
    msg: Uni.I18n.translate('general.loading', 'UNI', 'Loading...')
});