Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceDataValidationRulesSetMainView',
    itemId: 'deviceDataValidationRulesSetMainView',
    mRID: null,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesSetGrid',
        'Mdc.view.setup.devicedatavalidation.RulesSetPreview',
        'Uni.view.container.PreviewContainer'
    ],
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: me.mRID,
                        toggle: 7
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('device.dataValidation.pageTitle', 'MDC', 'Data Validation'),
                items: [
                    {
                        xtype: 'panel',
                        ui: 'medium',
                        layout: 'column',
                        padding: '10 0 0 0',
                        title: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'deviceDataValidationStatusField',
                                columnWidth: 1,
                                labelAlign: 'left',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                                value: Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...')
                            },
                            {
                                xtype: 'button',
                                itemId: 'deviceDataValidationStateChangeBtn',
                                text: Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'),
                                action: ''

                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceDataValidationRulesSetGrid',
                            mRID: me.mRID
                        },
                        emptyComponent: {
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
                                    src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<h4>' + Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.title', 'MDC', 'No validation rule sets found') + '</h4><br>' +
                                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.subtitle', 'MDC', 'There are no validation rule sets. This could be because:') + '<ul>' +
                                                '<li>' + Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'MDC', 'No validation rule sets have been assigned to this device.') + '</li>' +
                                                '<li>' + Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'MDC', 'Validation rule sets have been assigned, but you do not have permission to view them.') + '</li></ul>'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'container',
                            itemId: 'deviceDataValidationRulesSetPreviewCt'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});