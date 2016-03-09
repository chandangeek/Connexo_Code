Ext.define('Mdc.view.setup.devicelogbooks.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbooksPreview',
    itemId: 'deviceLogbooksPreview',
    requires: [
        'Mdc.view.setup.devicelogbooks.PreviewForm',
        'Mdc.view.setup.devicelogbooks.ActionMenu',
        'Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView'
    ],
    layout: 'fit',
    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLogbooksActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'deviceLogbooksPreviewForm'
        };
        me.callParent(arguments)
    },

    setLogbook: function(logbookRecord) {
        this.setTitle(logbookRecord.get('name'));
        this.down('#deviceLogbooksActionMenu').record = logbookRecord;
        this.down('#deviceLogbooksPreviewForm').loadRecord(logbookRecord);
    }
});
