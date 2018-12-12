/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.alarms-creation-rules-edit-action',
    requires: [
        'Dal.view.creationrules.EditActionForm'
    ],
    isEdit: false,
    returnLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'alarms-creation-rules-edit-action-form',
                itemId: 'alarms-creation-rules-edit-action-form',
                title: me.router.getRoute().getTitle(),
                ui: 'large',
                isEdit: me.isEdit,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});