/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.associated-metrology-configuration',
    itemId: 'associated-metrology-configuration',

    requires: [
        'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],

    //title: Uni.I18n.translate('usagepoint.linked-metrologyconfiguration', 'IMT', 'Associated Metrology configuration'),
    router: null,
    parent: null,
    //ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                name: 'name',
                labelWidth: 250,
                itemId: 'fld-mc-name',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                xtype: 'custom-attribute-sets-placeholder-form',
                inline: true,
                parent: me.parent,
                itemId: 'metrology-custom-attribute-sets-placeholder-form-id',
                actionMenuXtype: 'usage-point-setup-action-menu',
                attributeSetType: 'up',
                router: me.router
            }
        ];
        me.callParent(arguments);
    }
});