/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelPreview',
    maxHeight: 300,
    frame: true,
    editActionName: null,
    deleteActionName: null,
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.grid.column.ReadingType'
    ],

    initComponent: function () {
        var me = this;
        this.items = [
                     {
                         xtype: 'form',
                         name: 'loadProfileConfigurationChannelDetailsForm',
                         itemId: 'loadProfileConfigurationChannelDetailsForm',
                         layout: 'column',
                         defaults: {
                             xtype: 'container',
                             layout: 'form'

                         },
                         items: [
                             {
                                 defaults: {
                                     xtype: 'displayfield',
                                     labelWidth: 200
                                 },
                                 columnWidth: 0.6,
                                 items: [
                                     {
                                         xtype: 'displayfield',
                                         fieldLabel: Uni.I18n.translate('channelConfig.registerType', 'MDC', 'Register type'),
                                         name: 'registerTypeName'
                                     },
                                     {
                                         xtype: 'obis-displayfield',
                                         name: 'overruledObisCode'
                                     }
                                 ]
                             },
                             {
                                 columnWidth: 0.4,
                                 defaults: {
                                     xtype: 'displayfield',
                                     labelWidth: 200
                                 },
                                 items: [
                                     {
                                         fieldLabel: Uni.I18n.translate('channelConfig.overflowValue', 'MDC' ,'Overflow value'),
                                         name: 'overflowValue',
                                         renderer: function (value) {
                                             return value ? value : '-';
                                         }
                                     },
                                     {
                                         fieldLabel: Uni.I18n.translate('channelConfig.numberOfFractionDigits', 'MDC' ,'Number of fraction digits'),
                                         name: 'nbrOfFractionDigits'
                                     },
                                     {
                                         fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                         name: 'useMultiplier',
                                         renderer: function (value) {
                                             return value
                                                 ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                                 : Uni.I18n.translate('general.no', 'MDC', 'No')
                                         }
                                     },
                                     {
                                         xtype: 'reading-type-displayfield',
                                         fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                                         name: 'collectedReadingType'
                                     },
                                     {
                                         xtype: 'reading-type-displayfield',
                                         fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                                         name: 'calculatedReadingType',
                                         itemId: 'mdc-channel-config-preview-calculated',
                                         hidden: true
                                     },
                                     {
                                         fieldLabel: Uni.I18n.translate('general.endOfInterval', 'MDC', 'End of interval'),
                                         name: 'endOfInterval',
                                         itemId: 'mdc-channel-config-preview-end-of-interval',
                                         hidden: !me.isDailyProfile,
                                         renderer: function(value) {
                                               var timeUnitsStore = me.timeUnitsStore,
                                                   result = '',
                                                   timeUnit,
                                                   index;
                                               if (value !== null && value !== undefined) {
                                                   var hours = Math.floor(value/3600);
                                                   var minutes = Math.floor((value - hours * 3600)/60);
                                                   if (hours) {
                                                       index = timeUnitsStore.find('timeUnit', 'hours');
                                                       (index !== -1) && (timeUnit = timeUnitsStore.getAt(index).get('localizedValue'));
                                                       timeUnit && (result += hours + ' ' + timeUnit);
                                                   }
                                                   index = timeUnitsStore.find('timeUnit', 'minutes');
                                                   (index !== -1) && (timeUnit = timeUnitsStore.getAt(index).get('localizedValue'));
                                                   timeUnit && (result +=' '+ minutes + ' ' + timeUnit);
                                               }
                                               return result;
                                         }
                                     },
                                 ]
                             }
                         ]
                     }
                 ],
        this.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'menu',
                    plain: true,
                    border: false,
                    shadow: false,
                    items: [
                        {
                            text: Uni.I18n.translate('general.edit','MDC','Edit'),
                            action: this.editActionName
                        },
                        {
                            xtype: 'menuseparator',
                            action:'none'
                        },
                        {
                            text: Uni.I18n.translate('general.remove','MDC','Remove'),
                            action: this.deleteActionName
                        }
                    ]
                }
            }
        ];
        this.callParent(arguments);
    }

});