Ext.define('Mdc.view.setup.devicechannels.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreviewForm',
    itemId: 'deviceLoadProfileChannelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Mdc.view.setup.devicechannels.ValidationOverview',
        'Mdc.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],
    device: null,
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            layout: 'column',
            defaults: {
                xtype: 'form',
                columnWidth: 0.5,
                minWidth: 450
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: [
                                {
                                    xtype: 'reading-type-displayfield',
                                    fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                                    name: 'readingType',
                                    itemId: 'readingType',
                                    showTimeAttribute: false
                                },
                                {
                                    xtype: 'reading-type-displayfield',
                                    name: 'calculatedReadingType',
                                    itemId: 'calculatedReadingType',
                                    fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                                    showTimeAttribute: false,
                                    hidden: true
                                },
                                {
                                    xtype: 'obis-displayfield',
                                    name: 'overruledObisCode'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                                    name: 'multiplier',
                                    itemId: 'mdc-channel-preview-multiplier',
                                    hidden: true
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                                    name: 'interval_formatted'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                                    name: 'lastValueTimestamp_formatted'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('channelConfig.overflowValue', 'MDC', 'Overflow value'),
                                    name: 'overruledOverflowValue'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('channelConfig.numberOfFractionDigits', 'MDC' ,'Number of fraction digits'),
                                    name: 'overruledNbrOfFractionDigits'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                    name: 'useMultiplier',
                                    renderer: function(value) {
                                        return value
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.loadProfile', 'MDC', 'Load profile'),
                                    name: 'loadProfileId',
                                    renderer: function (value) {
                                        var res = '-',
                                            device;
                                        if (value instanceof Mdc.model.LoadProfileOfDevice) {
                                            var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({
                                                mRID: encodeURIComponent(me.device.get('mRID')),
                                                loadProfileId: value.get('id')
                                            });
                                            res = '<a href="' + url + '">' + Ext.String.htmlEncode(value.get('name')) + '</a>';
                                        } else if (Ext.isNumber(value)) {
                                            var loadProfile = Mdc.model.LoadProfileOfDevice;
                                            loadProfile.getProxy().setUrl(me.device.get('mRID'));
                                            loadProfile.load(value, {
                                                success: function (record) {
                                                    me.down('[name=loadProfileId]').setValue(record);
                                                }
                                            })
                                        }
                                        return res
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'deviceloadprofilechannelsoverview-validation',
                            router: me.router
                        }
                    ]
                },
                {
                    xtype: 'custom-attribute-sets-placeholder-form',
                    itemId: 'custom-attribute-sets-placeholder-form-id',
                    actionMenuXtype: 'deviceLoadProfileChannelsActionMenu',
                    attributeSetType: 'channel',
                    router: me.router
                }
            ]
        };

        me.callParent(arguments);
    }
});
