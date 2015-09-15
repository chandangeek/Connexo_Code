Ext.define('Mdc.customattributesets.view.AttributesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.administration-custom-attributes-grid',
    store: 'Mdc.customattributesets.store.Attributes',
    requires: [
        'Mdc.customattributesets.view.ActionMenu',
        'Uni.grid.column.CustomAttributeType'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.order', 'MDC', 'Order'),
                dataIndex: 'order'
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'MDC', 'Attribute name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.required', 'MDC', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                        Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                xtype: 'custom-attribute-type-column',
                dataIndex: 'customAttributeType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.default', 'MDC', 'Default'),
                dataIndex: 'defaultValue',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});

