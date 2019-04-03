/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.EventsIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Itk.view.LogGrid'
    ],
    alias: 'widget.events-issue-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'issue-preview',
                itemId: 'issue-detail-form',
                router: me.router,
                showTools: false,
                frame: false
            },
            {
                xtype: 'issue-details-log-grid',
                title: Uni.I18n.translate('general.relatedEvents', 'ITK', 'Related events'),
                itemId: 'issue-log-grid',
                store: me.store
            }
        ];

        me.callParent(arguments);
    }
});
