/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.window.CustomAttributeSetDetails', {
    extend: 'Uni.view.window.Notification',
    xtype: 'custom-attribute-set-details',
    record: null,

    requires: [
        'Uni.view.window.Notification',
        'Uni.view.form.CustomAttributeSetDetails'
    ],

    items: {
        xtype: 'custom-attribute-set-details-form',
        showDefaultTitle: false
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.setFormTitle(Uni.I18n.translate('customattributeset.customTitle', 'UNI', '\'{0}\' attribute set details', [encodeURIComponent(me.record.name)]));
        me.down('custom-attribute-set-details-form').loadCustomRecord(me.record);
    }
});