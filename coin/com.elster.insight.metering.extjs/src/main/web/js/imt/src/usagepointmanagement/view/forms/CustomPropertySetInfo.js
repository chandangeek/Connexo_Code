/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.CustomPropertySetInfo', {
    extend: 'Uni.property.form.Property',
    alias: 'widget.cps-info-form',
    predefinedRecord: null,

    defaults: {
        labelWidth: 260,
        width: 320
    },

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                itemId: 'restore-property-form-values-button',
                text: Uni.I18n.translate('general.restoreToDefault', 'IMT', 'Restore to default'),
                iconCls: 'icon-rotate-ccw3',
                iconAlign: 'left',
                handler: function () {
                    me.restoreAll();
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'container', // to fix wrong rendering
                items: {
                    itemId: 'cps-info-warning-' + me.navigationIndex,
                    xtype: 'uni-form-error-message',
                    dock: 'top',
                    width: me.defaults.labelWidth + me.defaults.width + 15,
                    hidden: true
                }
            }
        ];

        me.callParent(arguments);
        if (me.predefinedRecord) {
            me.loadRecord(me.predefinedRecord);
        }
    }
});