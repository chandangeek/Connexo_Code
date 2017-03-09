Ext.define('Dal.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.alarm-bulk-step5',
    title: Uni.I18n.translate('alarm.status','DAL','Status'),

    initComponent: function () {
        this.callParent(arguments);
    }
});