/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.attributes.CustomAttributeSetForm', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    requires: [
        'Uni.property.form.Property',
        'Imt.usagepointmanagement.view.forms.attributes.CustomAttributeSetDisplayForm'
    ],
    alias: 'widget.custom-attribute-set-form',

    initComponent: function () {
        var me = this;

        me.viewForm = {
            xtype: 'custom-attribute-set-display-form',
            itemId: 'view-form',
            isEdit: false,
            viewDefaults: me.viewDefaults,
            router: me.router
        };

        me.editForm = {
            xtype: 'property-form',
            itemId: 'edit-form'
        };

        me.callParent();
    }
});