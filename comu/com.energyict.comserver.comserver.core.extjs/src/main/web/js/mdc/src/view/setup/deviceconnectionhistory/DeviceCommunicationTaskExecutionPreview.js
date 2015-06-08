Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceCommunicationTaskExecutionPreview',
    itemId: 'deviceCommunicationTaskExecutionPreview',
    requires: [
        'Uni.form.field.Duration'
    ],
    layout: {
        type: 'vbox'
    },
    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text:  Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: [
            ]
        }
    ],

    items: [
        {
            xtype: 'form',
            border: false,
            layout: {
                type: 'column'
//                align: 'stretch'
            },
            itemId: 'deviceCommunicationTaskExecutionPreviewForm',
//            layout: {
//                type: 'vbox',
//                align: 'stretch'
//            },
//            items: [
//                {
//                    columnWidth: 0.50,
//                    xtype: 'container',
//                    layout: {
//                        type: 'column'
////                        align: 'stretch'
//                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.name', 'MDC', 'Name'),
                                        itemId: 'name'
                                    },
                                {
                                    xtype: 'displayfield',
                                    name: 'comTasks',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.comTasks', 'MDC', 'Communication task(s)'),
                                    itemId: 'comTasks',
                                    htmlEncode: false,
                                    renderer: function(value){
                                        if(value!==''){
                                            var result = '';
                                            Ext.each(value, function(item){
                                                result = result + '<li>'+ Ext.String.htmlEncode(item.name)+'</li>'
                                            });
                                            return result;
                                        } else {
                                            return '';
                                        }

                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'device',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.device', 'MDC', 'Device'),
                                    itemId: 'device',
                                    renderer: function(device){
                                            return device!==''?'<a href="#/devices/'+device.id+'">' + Ext.String.htmlEncode(device.name) + '</a>':'';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceType',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.deviceType', 'MDC', 'Device type'),
                                    itemId: 'deviceType',
                                    renderer: function(deviceType){
                                        return deviceType!==''?'<a href="#/administration/devicetypes/'+deviceType.id+'">' + Ext.String.htmlEncode(deviceType.name) + '</a>':'';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfiguration',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.deviceConfiguration', 'MDC', 'Device configuration'),
                                    itemId: 'deviceConfiguration',
                                    renderer: function(deviceConfiguration){
                                        return deviceConfiguration!=''?'<a href="#/administration/devicetypes/'+deviceConfiguration.deviceTypeId+'/deviceconfigurations/' + deviceConfiguration.id+'">' + Ext.String.htmlEncode(deviceConfiguration.name) + '</a>':'';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comScheduleName',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.schedule', 'MDC', 'Schedule'),
                                    itemId: 'comScheduleName'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comScheduleFrequency',
                                    itemId: 'comScheduleFrequency',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.frequency', 'MDC', 'Frequency'),
                                    renderer: function(value){
                                        return value!=''?Mdc.util.ScheduleToStringConverter.convert(value) || Uni.I18n.translate('general.undefined', 'MDC', 'Undefined'):'';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'urgency',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.urgency', 'MDC', 'Urgency'),
                                    itemId: 'urgency'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'alwaysExecuteOnInbound',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.alwaysExecuteOnInbound', 'MDC', 'Always execute on inbound'),
                                    itemId: 'alwaysExecuteOnInbound',
                                    renderer: function(value){
                                        return value?Uni.I18n.translate('general.yes', 'MDC', 'Yes'):Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.50,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'result',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                                    itemId: 'resultLink'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startTime',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                                    itemId: 'startedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishTime',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'uni-form-field-duration',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    usesSeconds: true
                                }
                            ]
                        }
                    ]
                }
//            ]
//        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});




