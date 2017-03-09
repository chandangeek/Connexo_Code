Ext.define('Dal.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.alarm-bulk-step1',
    title: Uni.I18n.translate('alarms.selectAlarms','DAL','Select alarms'),

    requires: [
        'Uni.util.FormErrorMessage',
        'Dal.view.bulk.AlarmsSelectionGrid'
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
            xtype: 'alarms-selection-grid',
            itemId: 'grd-alarms-selection'
        },
        {
            xtype: 'component',
            itemId: 'selection-grid-error',
            cls: 'x-form-invalid-under',
            margin: '-30 0 0 0',
            html: Uni.I18n.translate('alarms.selectAlarms.selectionGridError', 'DAL', 'Select at least one alarm'),
            hidden: true
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});