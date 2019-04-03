Ext.define('Itk.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-bulk-step3',
    title: Uni.I18n.translate('issue.actionDetails','ITK','Action details'),

    requires: [
        'Isu.view.issues.CloseForm',
        'Isu.view.issues.AssignIssue',
        'Isu.view.issues.SetPriority',
        'Isu.view.issues.SnoozeBulkForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});