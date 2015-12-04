Ext.define('Imt.validation.view.UsagePointDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'usagepoint-data-validation-panel',

    overflowY: 'auto',
    itemId: 'usagePointDataValidationPanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('usagePoint.dataValidation', 'IMT', 'Data validation'),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'form',
                        flex: 1,
                        itemId: 'usagePointDataValidationForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150,
                            style: {
                                marginRight: '20px',
                                padding: '20px'
                            },
                            flex: 1
                        },
                        items: [
                            {
                                itemId: 'statusField',
                                fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                                name: 'isActive',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                                }
                            },
                            {
                                itemId: 'allDataValidatedField',
                                fieldLabel: Uni.I18n.translate('usagepoint.registerData.allDataValidated', 'IMT', 'All data validated'),
                                name: 'allDataValidated',
                                htmlEncode: false,
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') :
                                        Uni.I18n.translate('general.no', 'IMT', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'fld-validation-result',
                                fieldLabel: Uni.I18n.translate('usagepoint.dataValidation.validationResult', 'IMT', 'Validation result'),
                                items: [
                                    {
                                        xtype: 'button',
                                        name: 'validationResultName',
                                        text: Uni.I18n.translate('usagepoint.dataValidation.validationResult', 'IMT', 'Validation result'),
                                        itemId: 'lnk-validation-result',
                                        ui: 'link',
                                        href: '#'

                                    }

                                ]

                            },
                            {
                                fieldLabel: Uni.I18n.translate('usagepoint.lastValidation', 'IMT', 'Last validation'),
                                itemId: 'lastValidationCont',
                                name: 'lastChecked',
                                renderer: function (value) {
                                    var icon = '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px" class="uni-icon-info-small" data-qtip="'
                                            + Uni.I18n.translate('usagepoint.lastValidation.tooltip', 'IMT', 'The moment when the validation ran for the last time.')
                                            + '"></span>',
                                        text = value ? Uni.DateTime.formatDateTimeLong(value) : Uni.I18n.translate('general.never', 'IMT', 'Never');

                                    return text + icon;
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();

    }
});


