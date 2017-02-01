/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.custom-attribute-sets-placeholder-form',

    requires: [
        'Mdc.customattributesonvaluesobjects.view.AttributeSetPropertyForm',
        'Mdc.customattributesonvaluesobjects.service.ActionMenuManager'
    ],

    flex: 1,
    actionMenuXtype: null,
    router: null,

    loadStore: function(store) {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll();
        if (me.actionMenuXtype) {
            Mdc.customattributesonvaluesobjects.service.ActionMenuManager.removePrevious(me.actionMenuXtype);
        }
        store.each(function (record) {
            me.add({
                xtype: 'custom-attribute-set-property-form',
                record: record,
                router: me.router,
                attributeSetType: me.attributeSetType,
                actionMenuXtype: me.actionMenuXtype,
                flex: 1
            })
        });
        Ext.resumeLayouts(true);
    }
});