Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.EmptyGridContainer'
    ],
    alias: 'widget.loadProfileTypesAddToDeviceTypeSetup',
    itemId: 'loadProfileTypesAddToDeviceTypeSetup',
    store: 'LoadProfileTypesOnDeviceTypeAvailable',

    intervalStore: null,
    deviceTypeId: null,

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('loadprofiletype.addloadprofiletypes', 'MDC', 'Add load profile types'),
            itemId: 'addLoadProfileTypePanel',
            items: [
                {
                    itemId: 'add-loadprofile-type-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    width: 380
                }

            ]

        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.down('panel').add(
            {
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'loadProfileTypesAddToDeviceTypeGrid',
                    itemId: 'loadprofile-type-add-grid',
                    intervalStore: me.intervalStore,
                    deviceTypeId: me.deviceTypeId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('loadprofiletypes.empty.title', 'MDC', 'No load profile types found'),
                    reasons: [
                        Uni.I18n.translate('loadprofiletypes.empty.list.item1', 'MDC', 'No load profile types are defined yet'),
                        Uni.I18n.translate('loadprofiletypes.empty.list.item2', 'MDC', 'All load profile types are already added to the device type.')
                    ]
                },
                onLoad: function (store, records) {
                    this.up('#addLoadProfileTypePanel').down('#addButton').setVisible(records && records.length);
                    this.getLayout().setActiveItem(!(records && records.length) ? this.getEmptyCt() : this.getGridCt());
                }

            },
            {
                xtype: 'container',
                itemId: 'add-loadprofile-type-selection-error',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('loadprofiletypes.no.loadprofiletypes.selected', 'MDC', 'Select at least 1 load profile type') + '</span>'
            },
            {
                xtype: 'form',
                border: false,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'toolbar',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                action: 'addLoadProfileTypeAction',
                                ui: 'action',
                                itemId: 'addButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                action: 'cancel',
                                xtype: 'button',
                                ui: 'link',
                                listeners: {
                                    click: {
                                        fn: function () {
                                            window.location.href = '#/administration/devicetypes/' + this.up('loadProfileTypesAddToDeviceTypeSetup').deviceTypeId + '/loadprofiles'
                                        }
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        );
    }
});

