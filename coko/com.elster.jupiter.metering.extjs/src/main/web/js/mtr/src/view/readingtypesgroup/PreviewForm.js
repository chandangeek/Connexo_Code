/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('readingtypes.name', 'MTR', 'Commodity'),
                name: 'commodity'
            }
        ];
        me.callParent(arguments);
    }
});