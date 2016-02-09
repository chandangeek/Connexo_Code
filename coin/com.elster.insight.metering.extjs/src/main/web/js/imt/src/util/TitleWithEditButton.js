Ext.define('Imt.util.TitleWithEditButton', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.title-with-edit-button',
    title: null,
    editHandler:null,
    record: null,
    hiddenBtn: false,
    items: [],

    initComponent: function() {
        var me = this;
        console.log(me.hiddenBtn);

        me.items = [
            '<label class="x-form-item-label x-form-item-label-top">' + me.title + '</label>',
            {
                xtype: 'button',
                itemId: 'pencil-btn',
                width: '16px',
                icon: '../imt/resources/images/pencil.png',
                cls: 'uni-btn-transparent masterfield-btn',
                tooltip: Uni.I18n.translate('general.tooltip.edit', 'IMT', 'Edit'),
                style: {
                    display: 'inline-block',
                    textDecoration: 'none !important',
                    position: 'absolute',
                    top: '8px'
                },
                hidden: me.hiddenBtn,
                handler: me.editHandler
            }
        ];
        me.callParent(arguments);
    }
});