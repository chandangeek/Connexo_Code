/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.devicemanagement.view.DeviceAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesFormMain',
    itemId: 'deviceAttributesFormMain',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID')
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
                    },
                    {
                        name: 'description',
                        itemId: 'fld-up-description',
                        fieldLabel: Uni.I18n.translate('general.label.description', 'IMT', 'Description')
                    },
                    {
                        name: 'aliasName',
                        itemId: 'fld-up-aliasName',
                        fieldLabel: Uni.I18n.translate('general.label.aliasName', 'IMT', 'Alias name')
                    },
                    {
                        name: 'serialNumber',
                        itemId: 'fld-up-serialNumber',
                        fieldLabel: Uni.I18n.translate('general.label.serialNumber', 'IMT', 'Serial number')
                    },
                    {
                        name: 'utcNumber',
                        itemId: 'fld-up-utcNumber',
                        fieldLabel: Uni.I18n.translate('general.label.utcNumber', 'IMT', 'UTC number')
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('general.label.version', 'IMT', 'Version')
                    },
                    {
                        name: 'installedDate',
                        itemId: 'fld-up-installedDate',
                        fieldLabel: Uni.I18n.translate('general.label.installedDate', 'IMT', 'Installed date'),
                        renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
 //                      			this.hide(); 
                       			return '-';
                       		}
                        }
                    },
                    {
                        name: 'removedDate',
                        itemId: 'fld-up-removedDate',
                        fieldLabel: Uni.I18n.translate('general.label.removedDate', 'IMT', 'Removed date'),
                        renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
                      			this.hide(); //return '-';
                       		}
                        }
                    },
                    {
                        name: 'retiredDate',
                        itemId: 'fld-up-retiredDate',
                        fieldLabel: Uni.I18n.translate('general.label.retiredDate', 'IMT', 'Retired date'),
                         renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
                      			this.hide(); //return '-';
                       		}
                        }
                    },

                    {
                        name: 'eMail1',
                        itemId: 'fld-up-eMail',
                        fieldLabel: Uni.I18n.translate('general.label.eMail1', 'IMT', 'E-Mail 1')
                    },
                    {
                        name: 'amrSystemName',
                        itemId: 'fld-up-amrSystemName',
                        fieldLabel: Uni.I18n.translate('general.label.amrSystemName', 'IMT', 'AMR system name')
                    },
                    {
                        name: 'usagePointName',
                        itemId: 'fld-up-usagePointName',
                        fieldLabel: Uni.I18n.translate('general.label.usagePointName', 'IMT', 'Usage point'),
                        renderer: function (value) {
                            if (value) {
                                var url = me.router.getRoute('usagepoints/view').buildUrl({usagePointId: value});
                                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            }
                            return '-';
                        }
                    }
                ]
            }
        ];
        me.callParent();
    }
});