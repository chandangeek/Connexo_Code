/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Est.estimationrules.view.EditForm'
    ],
    alias: 'widget.estimation-rule-edit',
    itemId: 'estimation-rule-edit',
    edit: false,
    returnLink: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                items: [
                    {
                        xtype: 'estimation-rule-edit-form',
                        itemId: 'estimation-rule-edit-form',
                        edit: me.edit,
                        returnLink: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});