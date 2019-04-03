Ext.define('Itk.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-bulk-step5',
    title: Uni.I18n.translate('issue.status','ITK','Status'),

    initComponent: function () {
        this.callParent(arguments);
    }
});