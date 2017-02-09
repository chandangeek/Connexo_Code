/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.window.CustomAttributeTypeDetails', {
    extend: 'Uni.view.window.Notification',
    xtype: 'custom-attribute-type-details',

    initComponent: function() {
        var me = this,
            possibleItems = [{
                xtype: 'container',
                margin : '0 0 20 0',
                html: Uni.I18n.translate('customattributetype.description', 'UNI', 'This attribute could contain one of following values:')
            }];

        Ext.each(me.possibleValues, function(value) {
            possibleItems.push({
                xtype: 'container',
                margin : '0 0 5 15',
                html: '- ' + value
            });
        });

        me.items = [{
            xtype:'panel',
            ui: 'medium',
            items: possibleItems
        }];

        me.callParent(arguments);
        me.setFormTitle(Uni.I18n.translate('customattributetype.title', 'UNI', 'Attribute details'));
    }
});