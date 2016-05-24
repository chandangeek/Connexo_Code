Ext.define('Mdc.filemanagement.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.files-devicetype-preview-container',
    deviceTypeId: null,

    requires: [
        'Mdc.filemanagement.view.FilesGrid'
    ],



    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'files-grid',
            itemId: 'files-grid',
            deviceTypeId: me.deviceTypeId,
            timeOfUseAllowed: me.timeOfUseAllowed
        };
            me.callParent(arguments);
    }
});