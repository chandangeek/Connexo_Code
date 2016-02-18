Ext.define('Mdc.view.setup.comtasks.ComtaskActionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskActionPreview',
    title: ' ',
    frame: true,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionActionMenu',
        'Mdc.view.setup.comtasks.ComtaskActionPreviewForm'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions','MDC','Actions'),
            privileges: Mdc.privileges.Communication.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'comtaskActionActionMenu'
            }
        }
    ],
    items: {
        xtype: 'comtaskActionPreviewForm',
        itemId: 'mdc-comtask-action-preview-form'
    }
});
