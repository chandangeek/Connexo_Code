Ext.define('Mdc.view.setup.registergroup.RegisterGroupEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupEdit',
    itemId: 'registerGroupEdit',

    requires: [
        //'Mdc.model.RegisterType'
        'Mdc.store.RegisterTypes'
        //'Ext.grid.PagingScroller'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    edit: false,
    autoScroll: true,
    isEdit: function () {
        return this.edit;
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
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerGroupEditCreateTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'registerGroupEditForm',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
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
                                        maxWidth: 650,
                                        maxLength: 80,
                                        enforceMaxLength: true
                                    },
                                    {
                                        xtype: 'label',
                                        itemId: 'separator',
                                        margin: '10 10 10 265',
                                        html: '<hr>'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'selectedRegisterTypes',
                                        msgTarget: 'under',
                                        itemId: 'editRegisterGroupSelectedField',
                                        fieldLabel: Uni.I18n.translate('registerGroup.registerTypes', 'MDC', 'Register types'),
                                        value: Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', 'Register types selected'),
                                        required: true
                                    },
                                    {
                                        xtype: 'emptygridcontainer',
                                        itemId: 'registerEditEmptyGrid',
                                        grid: {
                                            xtype: 'gridpanel',
                                            maxWidth: 950,
                                            maxHeight: 650,
                                            margin: '10 10 10 265',
                                            itemId: 'editRegisterGroupGridField',
                                            bodyBorder: true,
                                            columnLines: false,
                                            enableColumnHide: false,
                                            enableColumnMove: false,
                                            enableColumnResize: false,
                                            sortableColumns: false,
                                            // TODO: uncomment this to activate infinite scrolling when JP-2844 is fixed
                                            /*store: new Ext.data.Store({
                                             model: 'Mdc.model.RegisterType',
                                             buffered: true,
                                             leadingBufferZone: 20,
                                             trailingBufferZone: 20,
                                             pageSize: 20,
                                             autoLoad: true,
                                             proxy: {
                                             type: 'rest',
                                             url: '../../api/dtc/registertypes',
                                             reader: {
                                             type: 'json',
                                             root: 'registerTypes',
                                             totalProperty: 'total'
                                             }
                                             }
                                             }),*/
                                            store: 'RegisterTypes',
                                            selModel: {
                                                mode: 'MULTI',
                                                checkOnly: true,
                                                ignoreRightMouseSelection: true
                                            },
                                            selType: 'checkboxmodel',
                                            columns: [
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
                                                    header: Uni.I18n.translate('registerMappings.readingType', 'MDC', 'Reading type'),
                                                    items: [
                                                        {
                                                            icon: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
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
                                        emptyComponent: {
                                            xtype: 'container',
                                            layout: {
                                                type: 'hbox',
                                                align: 'left'
                                            },
                                            padding: '10 10 10 0',
                                            minHeight: 20,
                                            items: [
                                                {
                                                    xtype: 'image',
                                                    margin: '0 10 0 0',
                                                    src: "../mdc/resources/images/information.png",
                                                    height: 20,
                                                    width: 20
                                                },
                                                {
                                                    xtype: 'container',
                                                    items: [
                                                        {
                                                            xtype: 'component',
                                                            html: '<b>' + Uni.I18n.translate('registerGroupPreview.empty.title', 'MDC', 'No register types found') + '</b><br>' +
                                                                Uni.I18n.translate('registerGroupPreview.empty.detail', 'MDC', 'There are no register types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                                Uni.I18n.translate('registerGroupEdit.empty.list.item1', 'MDC', 'No register types have been defined yet.') + '</li></lv><br>' +
                                                                Uni.I18n.translate('registerGroupPreview.empty.steps', 'MDC', 'Possible steps:')
                                                        },
                                                        {
                                                            xtype: 'button',
                                                            itemId: 'createRegisterType',
                                                            margin: '10 0 0 0',
                                                            text: Uni.I18n.translate('registerType.addRegisterType', 'MDC', 'Add register type'),
                                                            action: 'createRegisterType'
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',
                                        itemId: 'btnsContainer',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
                                                xtype: 'button',
                                                ui: 'action',
                                                action: 'createAction',
                                                itemId: 'createEditButton'
                                            },
                                            {
                                                xtype: 'button',
                                                ui: 'link',
                                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                                itemId: 'cancelLink'
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
