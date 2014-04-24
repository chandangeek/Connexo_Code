Ext.define('Mdc.view.setup.registergroup.RegisterGroupEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerGroupEdit',
    itemId: 'registerGroupEdit',
    requires: [
        'Mdc.model.RegisterType'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
    edit: false,
    checked: 0,
    isEdit: function () {
        return this.edit
    },
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'saveRegisterGroup';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterGroup';
        }
    },

    initComponent: function () {
        this.items = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'breadcrumbTrail',
                        region: 'north',
                        padding: 6
                    },
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerGroupEditCreateTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'component',
                        html: '',
                        margins: '10 10 10 10',
                        itemId: 'registerGroupEditCreateInformation'
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'registerGroupEditForm',
                                padding: '10 10 0 10',
                                layout: {
                                    type: 'vbox'
                                },
//                    tbar: [
//                        {
//                            xtype: 'component',
//                            html: '<h4>Overview</h4>',
//                            itemId: 'deviceTypePreviewTitle'
//                        }
//                    ],
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('registerGroup.name', 'MDC', 'Name'),
                                        itemId: 'editRegisterGroupNameField',
                                        width: 650,
                                        maxLength: 80,
                                        enforceMaxLength: true
                                    },
                                    {
                                        xtype: 'box',
                                        autoEl: {tag: 'hr'}
                                    },
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'editRegisterGroupSelectedField',
                                        fieldLabel: Uni.I18n.translate('registerGroup.registerTypes', 'MDC', 'Register types'),
                                        value: Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', 'Register types selected'),
                                        required: true
                                    },
                                    {
                                        xtype: 'gridpanel',
                                        width: 950,
                                        height: 500,
                                        margins: '10 10 10 250',
                                        //flex: 1,
                                        //fieldLabel: Uni.I18n.translate('registerGroup.registerTypes', 'MDC', 'Register types'),
                                        //required: true,
                                        itemId: 'editRegisterGroupGridField',
                                        bodyBorder: true,
                                        columnLines: false,
                                        enableColumnHide: false,
                                        enableColumnMove: false,
                                        enableColumnResize: false,
                                        //hideHeaders: true,
                                        sortableColumns: false,
                                        store: new Ext.data.Store({
                                            model: 'Mdc.model.RegisterType'
                                        }),
                                        //data: [],
                                        columns: [
                                            {
                                                xtype: 'checkcolumn',
                                                dataIndex: 'selected',
                                                sortable: false,
                                                hideable: false,
                                                flex: 0.1
                                            },
                                            {
                                                header: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                                dataIndex: 'name',
                                                sortable: false,
                                                hideable: false,
                                                fixed: true,
                                                flex: 3
                                            },
                                            {
                                                xtype: 'actioncolumn',
                                                renderer: function (value, metaData, record) {
                                                    return '<div class="x-grid-cell-inner" style="float:left; font-size: 13px; line-height: 1em;">'
                                                        + record.getReadingType().get('mrid') + '&nbsp' + '&nbsp'
                                                        + '</div>'
                                                },
                                                header: Uni.I18n.translate('registerMappings.cimReadingType', 'MDC', 'CIM reading type'),
                                                items: [
                                                    {
                                                        icon: '../mdc/resources/images/information.png',
                                                        iconCls: 'uni-info-icon',
                                                        tooltip: Uni.I18n.translate('readingType.tooltip', 'MDC', 'Reading type info'),
                                                        handler: function (grid, rowIndex, colIndex, item, e) {
                                                            var record = grid.getStore().getAt(rowIndex);
                                                            this.fireEvent('showReadingTypeInfo', record);
                                                        }
                                                    }
                                                ],
                                                width: 300,
                                                tdCls: 'view',
                                                sortable: false,
                                                hideable: false

                                            },
                                            {
                                                header: Uni.I18n.translate('registerType.obisCode', 'MDC', 'OBIS code'),
                                                dataIndex: 'obisCode',
                                                sortable: false,
                                                hideable: false,
                                                fixed: true,
                                                flex: 1
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',
                                        //width: 430,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
                                                xtype: 'button',
                                                action: 'createAction',
                                                itemId: 'createEditButton'
                                                //  formBind: true
                                            },
                                            {
                                                xtype: 'component',
                                                padding: '3 0 0 10',
                                                itemId: 'cancelLink',
                                                autoEl: {
                                                    tag: 'a',
                                                    href: '#setup/registergroups/',
                                                    html: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }

                        ]
                    }


                ]
            }
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'saveRegisterGroup';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterGroup';
        }

    }


});
