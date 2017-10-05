Ext.define('Dal.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.alarm-bulk-step3',
    title: Uni.I18n.translate('alarm.actionDetails','DAL','Action details'),

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