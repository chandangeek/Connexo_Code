Ext.define('Imt.util.TitleWithEditButton', {
    extend: 'Ext.container.Container',
    alias: 'widget.title-with-edit-button',
    title: null,
    editHandler:null,
    record: null,
    hiddenBtn: false,
    layout: 'hbox',

    initComponent: function() {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                html: '<label class="x-form-item-label x-form-item-label-top">' + me.title + '</label>'
            },
            {
                xtype: 'button',
                itemId: 'pencil-btn',
                margin: '7 0 0 7',
                ui: 'plain',
                iconCls: 'icon-pencil2',
                tooltip: Uni.I18n.translate('general.tooltip.edit', 'IMT', 'Edit'),
                hidden: me.hiddenBtn,
                handler: me.editHandler
            }
        ];
        me.callParent(arguments);
    }
});