Ext.define('Mdc.view.setup.deviceloadprofilechannels.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreviewForm',
    itemId: 'deviceLoadProfileChannelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Mdc.view.setup.deviceloadprofilechannels.ValidationOverview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
                            name: 'name'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.unitOfMeasure', 'MDC', 'Unit of measure'),
                            name: 'unitOfMeasure_formatted'
                        },
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'),
                            name: 'cimReadingType'
                        },
                        {
                            xtype: 'obis-displayfield',
                            name: 'obisCode'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
                            name: 'multiplier'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.overflowValue', 'MDC', 'Overflow value'),
                            name: 'overflowValue'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm.nbrOfFractionDigits', 'MDC', 'Number of fraction digits'),
                            name: 'nbrOfFractionDigits'
                        }
                    ]
                },
                {
                    xtype: 'deviceloadprofilechannelsoverview-validation',
                    router: me.router
                }
            ]
        };

        me.callParent(arguments);
    }


});
