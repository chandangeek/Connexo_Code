/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step1',
    title: 'Select processes',//Uni.I18n.translate('issues.selectIssues','ISU','Select issues'),

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.processes.view.bulk.ProcessesSelectionGrid'
        //'Isu.view.issues.bulk.IssuesSelectionGrid'
    ],

    items: [
        {
            name: 'step1-errors',
            layout: 'hbox',
            hidden: true,
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message'
                }
            ]
        },
        {
            xtype: 'processes-selection-grid',
            itemId: 'processes-selection-grid'
        },
        {
            xtype: 'component',
            itemId: 'selection-grid-error',
            cls: 'x-form-invalid-under',
            margin: '-30 0 0 0',
            html: Uni.I18n.translate('issues.selectIssues.selectionGridError', 'ISU', 'Select at least one issue'),
            hidden: true
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
