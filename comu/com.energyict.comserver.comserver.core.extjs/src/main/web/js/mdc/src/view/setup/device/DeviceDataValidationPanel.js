Ext.define('Mdc.view.setup.device.DeviceDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'device-data-validation-panel',

    overflowY: 'auto',
    itemId: 'deviceDataValidationPanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('device.dataValidation', 'MDC', 'Data validation'),

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
                        itemId: 'deviceDataValidationForm',
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
                                fieldLabel: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                                name: 'isActive',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                                }
                            },
                            {
                                itemId: 'allDataValidatedField',
                                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                                name: 'allDataValidated',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                                        Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'fld-validation-result',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                                items: [
                                    {
                                        xtype: 'button',
                                        name: 'validationResultName',
                                        text: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                                        itemId: 'lnk-validation-result',
                                        ui: 'link',
                                        href: '#'

                                    }

                                ]

                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.lastValidation', 'MDC', 'Last validation'),
                                itemId: 'lastValidationCont',
                                name: 'lastChecked',
                                renderer: function (value) {
                                    var icon = '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px" class="uni-icon-info-small" data-qtip="'
                                            + Uni.I18n.translate('device.lastValidation.tooltip', 'MDC', 'The moment when the validation ran for the last time.')
                                            + '"></span>',
                                        text = value ? Uni.DateTime.formatDateTimeLong(value) : Uni.I18n.translate('general.never', 'MDC', 'Never');

                                    return text + icon;
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();

    },
    setValidationResult: function () {
        var me = this,
            href;

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'GET',
            timeout: 60000,

            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);

                if(res.loadProfileSuspectCount != 0 || res.registerSuspectCount != 0)
                    me.down('#lnk-validation-result').setText(Uni.I18n.translate('device.dataValidation.recentsuspects', 'MDC', 'Recent suspects'));
                else
                    me.down('#lnk-validation-result').setText(Uni.I18n.translate('device.dataValidation.recentsuspects', 'MDC', 'No suspects'));


            }
        });

    }

});


