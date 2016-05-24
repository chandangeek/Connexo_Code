Ext.define('Mdc.filemanagement.view.FilesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.files-grid',
    //store: 'Mdc.timeofuse.store.UsedCalendars',
    deviceTypeId: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.fileName', 'MDC', 'File name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.creationDate', 'MDC', 'Creation date'),
                dataIndex: 'creationDate',
                flex: 3
            },
            //{
            //    xtype: 'uni-actioncolumn',
            //    privileges: Mdc.privileges.DeviceType.view,
            //    isDisabled: me.fnIsDisabled,
            //    timeOfUseAllowed: me.timeOfUseAllowed,
            //    menu: {
            //        xtype: 'tou-devicetype-action-menu'
            //    },
            //    flex: 0.7
            //}
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                //store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('filemanagement.pagingtoolbartop.displayMsg', 'MDC', 'No files'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('filemanagement.addFile', 'MDC', 'Add file'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'add-file-btn'
                    }
                ],
                usesExactCount: true,
                noBottomPaging: true

            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function() {
        return !this.timeOfUseAllowed;
    }
});