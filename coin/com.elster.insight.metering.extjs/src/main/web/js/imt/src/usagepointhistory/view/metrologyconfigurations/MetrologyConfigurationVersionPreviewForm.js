/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-version-preview-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                itemId: 'fld-container-general-info',
                columnWidth: 0.4,
                items: [
                    {
                        name: 'current',
                        itemId: 'fld-current',
                        fieldLabel: Uni.I18n.translate('general.current', 'IMT', 'Current'),
                        renderer: function(value){
                            return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'period',
                        itemId: 'fld-period',
                        fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period')
                    },
                    {
                        name: 'metrologyConfiguration',
                        itemId: 'fld-metrology-configuration-name',
                        fieldLabel: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                        renderer: function(value){
                            if(value.id && value.name) {
                                if(Imt.privileges.MetrologyConfig.canView()){
                                    var url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: value.id});
                                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>'
                                } else {
                                    return Ext.String.htmlEncode(value.name);
                                }
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                columnWidth: 0.6,
                itemId: 'fld-container-active-purposes',
                labelWidth: 150,
                fieldLabel: Uni.I18n.translate('general.activePurposes', 'IMT', 'Active purposes'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    labelWidth: 150
                }
            }
        ];

        me.callParent();
    },

    loadPurposes: function (purposes) {
        var me = this;
        _.each(purposes, function(purpose, key){
            _.each(purpose, function(readingType, index){
                me.down('#fld-container-active-purposes').add({
                    xtype: 'reading-type-displayfield',
                    value: readingType,
                    itemId: 'fld-container-readingType-' + key + index,
                    fieldLabel: !index ? key : ' '
                })
            });
        })
    },

    loadOngoingProcesses: function (processes, count) {
        var me = this,
            url = me.router.getRoute('usagepoints/view/running').buildUrl();

        if(count){
            _.each(processes, function(process, index){
                me.down('#fld-container-general-info').add({
                    value: '[<a href="' + url + '">' + process.id + '</a>] -' + process.name,
                    itemId: 'fld-ongoing-process-' +  process.id,
                    fieldLabel: !index ? Uni.I18n.translate('general.ongoingProcesses', 'IMT', 'Ongoing processes') : ' '
                })
            })
        } else {
            me.down('#fld-container-general-info').add({
                value: '-',
                itemId: 'fld-ongoing-process-empty',
                fieldLabel: Uni.I18n.translate('general.ongoingProcesses', 'IMT', 'Ongoing processes')
            })
        }
    }
});

