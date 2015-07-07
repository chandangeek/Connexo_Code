Ext.define('Isu.view.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',

    requires: [
        'Uni.util.FormErrorMessage',
        'Isu.view.issues.bulk.IssuesSelectionGrid'
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
            xtype: 'issues-selection-grid'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});