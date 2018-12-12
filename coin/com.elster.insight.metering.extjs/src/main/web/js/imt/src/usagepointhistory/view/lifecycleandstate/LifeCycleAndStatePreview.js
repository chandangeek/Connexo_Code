/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStatePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.life-cycle-and-state-preview',
    xtype: 'life-cycle-and-state-preview',
    requires: [
        'Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStatePreviewForm'
    ],
    frame: true,
    items: {
        xtype: 'life-cycle-and-state-preview-form',
        itemId: 'life-cycle-and-state-preview-form'
    },

    loadRecord: function (record) {
        var me = this,
            form = me.down('#life-cycle-and-state-preview-form');

        Ext.suspendLayouts();
        me.setTitle(record.get('type_name'));
        form.loadRecord(record);
        me.addDynamicFields(record.get('microChecks'), form.down('#fld-pretransitionsContainer'));
        me.addDynamicFields(record.get('microActions'), form.down('#fld-autoActionsContainer'));
        Ext.resumeLayouts(true);
    },

    addDynamicFields: function (properties, container) {
        container.removeAll();
        if (properties && properties.length) {
            container.show();
            Ext.Array.each(properties, function (property) {
                container.add({
                    xtype: 'displayfield-with-info-icon',
                    fieldLabel: undefined,
                    value: property.id,
                    infoTooltip: property.name
                });
            });
        } else {
            container.hide();
        }
    }
});
