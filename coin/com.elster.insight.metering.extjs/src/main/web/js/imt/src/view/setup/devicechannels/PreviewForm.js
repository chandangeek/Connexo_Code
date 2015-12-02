Ext.define('Imt.view.setup.devicechannels.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreviewForm',
    itemId: 'deviceLoadProfileChannelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.view.setup.devicechannels.ValidationOverview',
        'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.view.setup.devicechannels.ActionMenu'
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
                            fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'IMT', 'General'),
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: [
                                {
                                    xtype: 'reading-type-displayfield',
                                    name: 'readingType',
                                    itemId: 'readingType',
                                    showTimeAttribute: false
                                },
                                {
                                    xtype: 'reading-type-displayfield',
                                    name: 'calculatedReadingType',
                                    itemId: 'calculatedReadingType',
                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.calculatedReadingType', 'IMT', 'Calculated reading type'),
                                    showTimeAttribute: false,
                                    hidden: true
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'IMT', 'Interval'),
                                    name: 'interval_formatted'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastReading', 'IMT', 'Last reading'),
                                    name: 'lastReading',
                                    renderer: function (value) {
                                        var tooltip = Uni.I18n.translate('deviceloadprofiles.tooltip.lastreading', 'IMT', 'The moment when the data was read out for the last time.');
                                        return value
                                            ? Uni.DateTime.formatDateTimeLong(value) + '<span style="margin: 0 0 0 10px; width: 16px; height: 16px" class="uni-icon-info-small" data-qtip="' + tooltip + '"></span>'
                                            : '';
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('device.channels.timestampLastValue', 'IMT', 'Timestamp last value'),
                                    name: 'lastValueTimestamp_formatted'
                                },
                                {
                                    xtype: 'obis-displayfield',
                                    name: 'obisCode'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.overflowValue', 'IMT', 'Overflow value'),
                                    name: 'overflowValue'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm.nbrOfFractionDigits', 'IMT', 'Number of fraction digits'),
                                    name: 'nbrOfFractionDigits'
                                },
//                                {
//                                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.loadProfile', 'IMT', 'Load profile'),
//                                    name: 'loadProfileId',
//                                    renderer: function (value) {
//                                        var res = '',
//                                            device;
//                                        if (value instanceof Imt.model.LoadProfileOfDevice) {
//                                            var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({
//                                                mRID: encodeURIComponent(me.device.get('mRID')),
//                                                loadProfileId: value.get('id')
//                                            });
//                                            res = '<a href="' + url + '">' + Ext.String.htmlEncode(value.get('name')) + '</a>';
//                                        } else if (Ext.isNumber(value)) {
//                                            var loadProfile = Imt.model.LoadProfileOfDevice;
//                                            loadProfile.getProxy().setUrl(me.device.get('mRID'));
//                                            loadProfile.load(value, {
//                                                success: function (record) {
//                                                    me.down('[name=loadProfileId]').setValue(record)
//                                                }
//                                            })
//                                        }
//                                        return res
//                                    }
//                                }
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
