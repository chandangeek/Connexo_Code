/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Sam.model.VersionInfo', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'connexoVersionInfo',
            mapping: function (data) {
                return Uni.I18n.translate('general.connexoVersion', 'SAM', 'Connexo Version: {0}', [data.CONNEXO_VERSION]);
        }
        }
        ]
});