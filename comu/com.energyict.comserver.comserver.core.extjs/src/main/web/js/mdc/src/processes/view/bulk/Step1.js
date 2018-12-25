/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step1',
    title: Uni.I18n.translate('mdc.processgrid.bulk.selectProcesses','MDC','Select processes'),

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.processes.view.bulk.ProcessesSelectionGrid'
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
            html: Uni.I18n.translate('mdc.processgrid.bulk.selectionGridError', 'MDC', 'Select at least one process'),
            hidden: true
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});