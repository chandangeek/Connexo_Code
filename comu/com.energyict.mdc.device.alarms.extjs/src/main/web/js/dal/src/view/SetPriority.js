/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.SetPriority', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.SetPriorityForm'
    ],
    alias: 'widget.alarm-set-priority',
    returnLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'set-priority-form',
                itemId: 'set-priority-form',
                title: Uni.I18n.translate('alarm.setpriority','DAL','Set priority'),
                ui: 'large',
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});

