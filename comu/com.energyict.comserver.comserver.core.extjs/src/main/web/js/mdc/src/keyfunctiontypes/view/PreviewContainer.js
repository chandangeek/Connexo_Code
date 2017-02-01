Ext.define('Mdc.keyfunctiontypes.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.key-function-types-preview-container',
    deviceTypeId: null,

    requires: [
        'Mdc.keyfunctiontypes.view.KeyFunctionTypesGrid',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.keyfunctiontypes.view.Preview'
    ],


    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'key-function-types-grid',
            itemId: 'key-function-types-grid',
            deviceTypeId: me.deviceTypeId
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-files',
            title: Uni.I18n.translate('keyfunctiontypes.empty.title', 'MDC', 'No key function types found'),
            reasons: [
                Uni.I18n.translate('keyfunctiontypes.empty.list.item', 'MDC', 'No key function types have been defined yet'),
            ],
            //stepItems: [
            //    {
            //        xtype: 'button',
            //        text: Uni.I18n.translate('keyfunctiontypes.addKeyFunctionType', 'MDC', 'Add key function type'),
            //        itemId: 'add-key-function-type'
            //    }
            //]
        };

        me.previewComponent = {
            xtype: 'key-function-types-preview',
            itemId: 'key-function-types-preview'
        };

        me.callParent(arguments);
    }
});