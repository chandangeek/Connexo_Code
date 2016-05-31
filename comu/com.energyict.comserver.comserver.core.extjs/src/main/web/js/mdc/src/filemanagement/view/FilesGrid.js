Ext.define('Mdc.filemanagement.view.FilesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.files-grid',
    store: 'Mdc.filemanagement.store.Files',
    deviceTypeId: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop'
    ],
    forceFit: true,

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
                flex: 3,
                renderer: function (value) {
                    return value && value !== 0 ? Uni.DateTime.formatDateTime(new Date(value), Uni.DateTime.LONG, Uni.DateTime.LONG) : '-';
                }
            },
            {
                xtype: 'actioncolumn',
                align: 'right',
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                items: [
                    {
                        iconCls: 'uni-icon-delete',
                        itemId: 'apr-remove-import-service-btn',
                        tooltip: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        handler: function (grid, rowIndex, colIndex, column, event, record) {
                            this.fireEvent('removeEvent', record);
                        }
                    }
                ]
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('filemanagement.pagingtoolbartop.displayMsg', 'MDC', 'No files'),
                items: [
                    {
                        style: {
                            marginBottom: '0'
                        },
                        xtype: 'form',
                        autoEl: {
                            tag: 'form',
                            enctype: 'multipart/form-data'
                        },
                        items: [{
                            xtype: 'filefield',
                            name: 'uploadField',
                            style: {
                                marginBottom: '0'
                            },
                            buttonText: Uni.I18n.translate('filemanagement.addFile', 'MDC', 'Add file'),
                            privileges: Mdc.privileges.DeviceType.admin,
                            buttonConfig: {
                                ui: 'default',
                                style: {
                                    marginBottom: '0'
                                }
                            },
                            buttonOnly: true,
                            itemId: 'add-file-btn',
                            vtype: 'fileUpload'
                        }]
                    }
                ],
                usesExactCount: true,
                noBottomPaging: true

            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function () {
        return !this.timeOfUseAllowed;
    }
});