/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.DefaultIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.default-issue-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'connection-issue-details-container',
                xtype: 'data-collection-details-container'
            },
        ]

        me.callParent(arguments);
    }
});
