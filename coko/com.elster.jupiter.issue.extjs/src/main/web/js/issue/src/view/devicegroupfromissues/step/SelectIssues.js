/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.step.SelectIssues', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.select-issues-step',

    requires: [
        'Uni.util.FormErrorMessage',
        'Isu.view.devicegroupfromissues.grid.IssuesGrid'
    ],

    ui: 'large',

    isGridDataLoaded: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'step-errors',
                name: 'step-errors',
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
                itemId: 'prefiltered-issues-grid',
                xtype: 'prefiltered-issues-grid'
            },
            {
                itemId: 'issues-grid-validation-error',
                xtype: 'component',
                cls: 'x-form-invalid-under',
                margin: '-30 0 0 0',
                html: Uni.I18n.translate('devicegroupfromissues.wizard.step.selectIssues.grid.validationMsg', 'ISU', 'Select at least one issue'),
                hidden: true
            }
        ];

        this.callParent(arguments);
    }

});