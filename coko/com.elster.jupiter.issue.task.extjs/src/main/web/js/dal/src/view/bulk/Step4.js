Ext.define('Itk.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-bulk-step4',
    title: Uni.I18n.translate('issue.confirmation','ITK','Confirmation'),

    initComponent: function () {
        this.callParent(arguments);
    }
});