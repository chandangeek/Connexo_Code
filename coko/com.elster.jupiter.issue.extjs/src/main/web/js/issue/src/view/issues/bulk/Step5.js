/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step5',
    title: Uni.I18n.translate('issue.status','ISU','Status'),

    initComponent: function () {
        this.callParent(arguments);
    }
});