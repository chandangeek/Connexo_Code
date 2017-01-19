Ext.define('Uni.view.grid.PendingChangesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.pendingChangesGrid',
    requires: [
        'Uni.store.PendingChanges'
    ],
    viewConfig: {
        disableSelection: true,
        enableTextSelection: true
    },

    store: 'Uni.store.PendingChanges',

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.attribute', 'UNI', 'Attribute'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'attributeName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.originalValue', 'UNI', 'Original value'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'originalValue',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.newValue', 'UNI', 'New value'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'newValue',
                flex: 1
            }
        ];
        me.callParent(arguments);
    }

});