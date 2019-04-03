/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.SetPriority', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.SetPriorityForm'
    ],
    alias: 'widget.issue-set-priority',
    returnLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'set-priority-form',
                itemId: 'set-priority-form',
                title: Uni.I18n.translate('issue.setpriority','ITK','Set priority'),
                ui: 'large',
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});

