Ext.define('Itk.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-bulk-step1',
    title: Uni.I18n.translate('issues.selectIssues','ITK','Select issues'),

    requires: [
        'Uni.util.FormErrorMessage',
        'Itk.view.bulk.IssuesSelectionGrid'
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
            xtype: 'issues-selection-grid',
            itemId: 'grd-issues-selection'
        },
        {
            xtype: 'component',
            itemId: 'selection-grid-error',
            cls: 'x-form-invalid-under',
            margin: '-30 0 0 0',
            html: Uni.I18n.translate('issues.selectIssues.selectionGridError', 'ITK', 'Select at least one issue'),
            hidden: true
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});