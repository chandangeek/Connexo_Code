/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step4',
    title: Uni.I18n.translate('issue.confirmation','ISU','Confirmation'),

    initComponent: function () {
        this.callParent(arguments);
    }
});