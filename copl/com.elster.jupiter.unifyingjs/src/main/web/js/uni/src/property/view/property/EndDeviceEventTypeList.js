/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.EndDeviceEventTypeList', {
    extend: 'Uni.property.view.property.Base',

    requires: [
        'Uni.property.store.DeviceTypes',
        'Uni.property.store.DeviceDomains',
        'Uni.property.store.DeviceSubDomains',
        'Uni.property.store.DeviceEventOrActions',
        'Uni.property.store.EventTypesForAlarmRule'
    ],

    getEditCmp: function () {
        var me = this;
        return [
            {
                items: [
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'eventTypesContainer',
                        required: true,
                        layout: 'hbox',
                        msgTarget: 'under',
                        width: 1000,
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('general.noEventTypes','UNI','No event types have been added'),
                                itemId: 'noEventTypesLabel',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'gridpanel',
                                itemId: 'eventTypesGridPanel',
                                store: new Ext.create('Uni.property.store.EventTypesForAlarmRule'),
                                hideHeaders: true,
                                padding: 0,
                                scroll: 'vertical',
                                viewConfig: {
                                    disableSelection: true,
                                    enableTextSelection: true
                                },
                                columns: [
                                    {
                                        xtype: 'event-type-column',
                                        dataIndex: 'eventFilterCode',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'uni-actioncolumn-remove',
                                        align: 'right',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            if (grid.getStore().count() === 0) {
                                                me.updateEventTypesGrid();
                                            }
                                        }
                                    }
                                ],
                                width: 500,
                                height: 220
                            },
                            {
                                xtype: 'button',
                                itemId: 'eventTypeButton',
                                text: Uni.I18n.translate('general.addEventTypes', 'UNI', 'Add event types'),
                                margin: '0 0 0 10',
                                handler: function() {
                                    Ext.create('Uni.property.view.property.EventTypeWindow', {
                                        title: Uni.I18n.translate('general.addEventType', 'UNI', 'Add event type'),
                                        addEventType: me.addEventType,
                                        parent: me
                                    });
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    },

    initComponent: function(){
        var me = this;
        var modelEntry = Ext.create('Uni.property.model.EndDeviceEventTypePart', {
            value: -1,
            displayName: Uni.I18n.translate('general.all', 'UNI', 'All')
        });

        me.deviceTypesStore = Ext.getStore('Uni.property.store.DeviceTypes');
        me.deviceDomainsStore = Ext.getStore('Uni.property.store.DeviceDomains');
        me.deviceSubDomainsStore = Ext.getStore('Uni.property.store.DeviceSubDomains');
        me.deviceEventOrActionsStore = Ext.getStore('Uni.property.store.DeviceEventOrActions');
        me.deviceTypesStore.load(function () {
            me.deviceTypesStore.insert(0, modelEntry);
        });
        me.deviceDomainsStore.load(function () {
            me.deviceDomainsStore.insert(0, modelEntry);
        });
        me.deviceSubDomainsStore.load(function () {
            me.deviceSubDomainsStore.insert(0, modelEntry);
        });
        me.deviceEventOrActionsStore.load(function () {
            me.deviceEventOrActionsStore.insert(0, modelEntry);
        });
        this.callParent(arguments);
    },

    setValue: function (value) {
        var me = this,
            eventTypePanel = me.down('#eventTypesGridPanel');

        if(value && value.length > 0){
            Ext.Array.forEach(value, function (code) {
                var eventCode = code.split(':')[0];
                var deviceCode = code.split(':')[1];
                var deviceTypeName = '';
                var deviceDomainName = '';
                var deviceSubDomainName = '';
                var deviceEventOrActionNameName = '';
                me.deviceTypesStore.each(function(item){
                    if(item.get('value') == eventCode.split('.')[0]){
                        deviceTypeName = item.get('displayName');
                    }
                });
                me.deviceDomainsStore.each(function(item){
                    if(item.get('value') == eventCode.split('.')[1]){
                        deviceDomainName = item.get('displayName');
                    }
                });
                me.deviceSubDomainsStore.each(function(item){
                    if(item.get('value') == eventCode.split('.')[2]){
                        deviceSubDomainName = item.get('displayName');
                    }
                });
                me.deviceEventOrActionsStore.each(function(item){
                    if(item.get('value') == eventCode.split('.')[3]){
                        deviceEventOrActionNameName = item.get('displayName');
                    }
                });

                var model = {
                    eventFilterCode: deviceCode != '*' ? eventCode + ' (' + deviceCode + ')' : eventCode,
                    eventFilterCodeUnformatted: eventCode,
                    deviceTypeName: deviceTypeName,
                    deviceDomainName: deviceDomainName,
                    deviceSubDomainName: deviceSubDomainName,
                    deviceEventOrActionName: deviceEventOrActionNameName,
                    deviceCode: deviceCode == '*' ? '' : deviceCode
                };

                var eventTypeModel = Ext.create('Uni.property.model.EventTypeForAddAlarmRuleGrid', model);
                eventTypePanel.getStore().add(eventTypeModel);
            });

        }
        me.updateEventTypesGrid();
    },

    updateEventTypesGrid: function() {
        var me = this,
            eventTypesGridPanel = me.down('#eventTypesGridPanel'),
            noEventTypesLabel = me.down('#noEventTypesLabel');
        eventTypesGridPanel.setVisible(eventTypesGridPanel.getStore().count() !== 0);
        noEventTypesLabel.setVisible(eventTypesGridPanel.getStore().count() === 0);
    },

    addEventType: function(values, window){
        var me = this,
            eventTypePanel = me.down('#eventTypesGridPanel'),
            noEventTypePanel = me.down('#noEventTypesLabel'),
            eventTypeModel = Ext.create('Uni.property.model.EventTypeForAddAlarmRuleGrid', values);

        if (!window.isFormValid(eventTypePanel.getStore())) {
            return;
        }
        window.destroy();

        eventTypePanel.getStore().add(eventTypeModel);

        if (eventTypePanel.getStore().count() > 0) {
            noEventTypePanel.hide();
            eventTypePanel.show();
        }
    },
    
    getValue: function () {
        var me = this,
            eventTypePanel = me.down('#eventTypesGridPanel');

        var items = [];
        eventTypePanel.getStore().each(function(item) {
            var eventCode = '';
            eventCode += item.get('eventFilterCodeUnformatted') + ':';
            eventCode += item.get('deviceCode') == '' ? '*' : item.get('deviceCode');
            items.push(eventCode);
        });

        return items;
    }
});
