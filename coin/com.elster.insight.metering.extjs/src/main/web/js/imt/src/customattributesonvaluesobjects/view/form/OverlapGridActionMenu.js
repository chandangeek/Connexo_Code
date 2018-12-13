/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.form.OverlapGridActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.versions-overlap-grid-action-menu',
    itemId: 'versions-overlap-grid-action-menu-id',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('customattributesonvaluesobjects.alignleft', 'IMT', 'Align left'),
                itemId: 'custom-attribute-set-version-action-menu-align-left-btn-id',
                isVisible: function() {
                    return (me.record.index > 0)
                        && (me.store.getAt(me.record.index - 1).get('endTime'));
                },
                handler: function() {
                    var leftRecord = me.store.getAt(me.record.index - 1);
                    Ext.ComponentQuery.query('#custom-attribute-set-versions-overlap-grid-id')[0].fireEvent('alignleft', leftRecord.get('endTime'));
                }
            },
            {
                text: Uni.I18n.translate('customattributesonvaluesobjects.alignright', 'IMT', 'Align right'),
                itemId: 'custom-attribute-set-version-action-menu-align-right-btn-id',
                isVisible: function() {
                    var recordsAfter = me.store.getCount() - (me.record.index + 1);
                    return recordsAfter > 0;
                },
                handler: function() {
                    var rightRecord = me.store.getAt(me.record.index + 1);
                    Ext.ComponentQuery.query('#custom-attribute-set-versions-overlap-grid-id')[0].fireEvent('alignright', rightRecord.get('startTime'));
                }
            }
        ];

        me.callParent(arguments);
    },

    listeners: {
        //todo: code like this should be generalized in common component
        beforeshow: function () {
            var me = this;
            Ext.suspendLayouts();
            me.items.each(function (item) {
                if (item.isVisible === undefined) {
                    item.show();
                } else {
                    item.isVisible.call(me) ? item.show() : item.hide();
                }
            });

            Ext.resumeLayouts(true);
        }
    }
});