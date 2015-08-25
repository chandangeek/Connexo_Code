Ext.define('Mdc.view.setup.registergroup.RegisterGroupEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupEdit',
    itemId: 'registerGroupEdit',

    requires: [
        'Mdc.store.RegisterTypes',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType',
        'Ext.grid.plugin.BufferedRenderer'
    ],
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('registerGroup.edit', 'MDC', 'Edit register group'),

                items: [
                    {
                        xtype: 'container',
                        itemId: 'registerGroupEditCard',
                        layout: 'card',

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
                                        name: 'errors',
                                        ui: 'form-error-framed',
                                        itemId: 'registerGroupEditFormErrors',
                                        layout: 'hbox',
                                        margin: '0 0 10 0',
                                        hidden: true,
                                        defaults: {
                                            xtype: 'container'
                                        }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                        itemId: 'editRegisterGroupNameField',
                                        maxWidth: 650,
                                        maxLength: 80,
                                        enforceMaxLength: true
                                    },
                                    {
                                        xtype: 'label',
                                        itemId: 'separator',
                                        margin: '0 0 0 265',
                                        html: '<hr>'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'registerTypeInGroups',
                                        msgTarget: 'under',
                                        itemId: 'editRegisterGroupSelectedField',
                                        fieldLabel: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                                        value: Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', 'Register types selected'),
                                        required: true
                                    },
                                    {
                                        xtype: 'preview-container',
                                        itemId: 'registerEditEmptyGrid',
                                        grid: {
                                            xtype: 'gridpanel',
                                            margin: '0 0 0 265',
                                            itemId: 'editRegisterGroupGridField',
                                            bodyBorder: true,
                                            columnLines: false,
                                            enableColumnHide: false,
                                            enableColumnMove: false,
                                            enableColumnResize: false,
                                            sortableColumns: false,
                                            store: 'RegisterTypes',
                                            selModel: {
                                                mode: 'MULTI',
                                                showHeaderCheckbox: false,
                                                checkOnly: true,
                                                ignoreRightMouseSelection: true
                                            },
                                            selType: 'checkboxmodel',
                                            plugins: {
                                                ptype: 'bufferedrenderer',
                                                trailingBufferZone: 50,  // Keep 20 rows rendered in the table behind scroll
                                                leadingBufferZone: 100   // Keep 50 rows rendered in the table ahead of scroll
                                            },
                                            columns: [
                                                {
                                                    xtype: 'reading-type-column',
                                                    dataIndex: 'readingType',
                                                    flex: 2
                                                },
                                                {
                                                    xtype: 'obis-column',
                                                    dataIndex: 'obisCode',
                                                    flex: 1
                                                }
                                            ]
                                        },
                                        emptyComponent: {
                                            xtype: 'no-items-found-panel',
                                            title: Uni.I18n.translate('setup.registergroup.RegisterGroupEdit.NoItemsFoundPanel.title', 'MDC', 'No register types found'),
                                            reasons: [
                                                Uni.I18n.translate('setup.registergroup.RegisterGroupEdit.NoItemsFoundPanel.reason1', 'MDC', 'No register types are associated to this register group.')
                                            ]
                                        }
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                itemId: 'registerGroupAddButton',
                                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                                xtype: 'button',
                                                ui: 'action',
                                                action: 'save'
                                            },
                                            {
                                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                                xtype: 'button',
                                                ui: 'link',
                                                itemId: 'cancelLink',
                                                href: '#/administration/registergroups/'
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox',
                                    align: 'left'
                                },
                                minHeight: 20,
                                items: [
                                    {
                                        xtype: 'image',
                                        margin: '0 10 0 0',
                                        src: '../sky/build/resources/images/shared/icon-info-small.png',
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
                                                text: Uni.I18n.translate('registerType.addRegisterType', 'MDC', 'Add register types'),
                                                action: 'createRegisterType'
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
            this.down('#registerGroupAddButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
        } else {
            this.down('#registerGroupAddButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
        }
    }
});
