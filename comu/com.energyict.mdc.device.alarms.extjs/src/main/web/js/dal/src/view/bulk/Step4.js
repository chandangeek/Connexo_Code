Ext.define('Dal.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.alarm-bulk-step4',
    title: Uni.I18n.translate('alarm.confirmation','DAL','Confirmation'),

    initComponent: function () {
        this.callParent(arguments);
    }
});