Ext.define('Mdc.view.setup.comtasks.ComtaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskPreview',
    hidden: false,
    title: Uni.I18n.translate('general.details','MDC','Details'),
    frame: true,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionMenu',
        'Mdc.view.setup.comtasks.ComtaskPreviewForm'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions','MDC','Actions'),
            privileges: Mdc.privileges.Communication.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'comtaskActionMenu'
            }
        }
    ],
    items: {
        xtype: 'comtaskpreviewform',
        itemId: 'comtaskPreviewFieldsPanel'
    }
});