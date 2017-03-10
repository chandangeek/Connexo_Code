/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step2',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormEmptyMessage',
        'Mdc.model.Channel'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step2-errors',
                xtype: 'uni-form-error-message',
                width: 700,
                hidden: true
            },
            {
                xtype: 'container',
                itemId: 'mdc-dataloggerslave-link-wizard-step2-container',
                fieldLabel: '',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [

                ]
            }
        ];

        me.callParent(arguments);
    },

    initialize: function(loadProfileConfigurationRecords, dataLoggerChannelRecords, previouslyMappedChannels) {
        var me = this;
        if (me.rendered) {
            me.doInitialize(loadProfileConfigurationRecords, dataLoggerChannelRecords, previouslyMappedChannels);
        } else {
            me.on('afterrender', function() {
                me.doInitialize(loadProfileConfigurationRecords, dataLoggerChannelRecords, previouslyMappedChannels);
            }, me, {single:true});
        }
    },

    doInitialize: function(loadProfileConfigurationRecords, dataLoggerChannelRecords, previouslyMappedChannels) {
        var me = this,
            loadProfileCounter = 0,
            channelCounter = 0,
            form,
            slaveChannelsFound = false;

        // Check the existence of slave channels
        if (!Ext.isEmpty(loadProfileConfigurationRecords)) {
            Ext.Array.forEach(loadProfileConfigurationRecords, function (record) {
                slaveChannelsFound |= !Ext.isEmpty(record.get('channels'));
            }, me);
        }

        me.down('#mdc-dataloggerslave-link-wizard-step2-container').removeAll();
        if (!slaveChannelsFound) {
            Ext.suspendLayouts();
            me.down('#mdc-dataloggerslave-link-wizard-step2-container').add({
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('general.dataLoggerSlave.noChannels', 'MDC', 'There are no channels on the data logger slave.')
            });
            Ext.resumeLayouts(true);
            me.doLayout();
        } else {
            Ext.Array.forEach(loadProfileConfigurationRecords, function (record) {
                loadProfileCounter++;

                Ext.suspendLayouts();
                me.down('#mdc-dataloggerslave-link-wizard-step2-container').add({
                    xtype: 'container',
                    layout: {
                        type: 'vbox'
                    },
                    items: [
                        {
                            xtype: 'label',
                            html: '<b>' + record.get('name') + '</b>'
                        },
                        {
                            xtype: 'container',
                            width: 700,
                            margin: '20 0 0 0',
                            itemId: 'mdc-step2-form-' + loadProfileCounter,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 300
                            },
                            items: []
                        }
                    ]
                });
                Ext.resumeLayouts(true);
                me.doLayout();
                form = me.down('#mdc-step2-form-' + loadProfileCounter);

                Ext.Array.forEach(record.get('channels'), function (channel) {
                    channelCounter++;
                    form.add(
                        {
                            xtype: 'combobox',
                            listConfig: {
                                loadingText: null,
                                loadMask: false
                            },
                            editable: false,
                            forceSelection: true,
                            multiSelect: false,
                            queryMode: 'local',
                            fieldLabel: channel.name,
                            store: me.createChannelStore(record, dataLoggerChannelRecords),
                            emptyText: Uni.I18n.translate('general.channelCombo.emptyText', 'MDC', 'Select a channel...'),
                            displayField: 'extendedChannelName',
                            valueField: 'id',
                            msgTarget: 'under',
                            itemId: 'mdc-step2-channel-combo-' + channelCounter
                        }
                    );
                }, me);

            }, me);
        }

        // 1. (Pre)select combo items according to previously made choices
        // 2. (Pre)select a combo item if it's the only one available
        var i, channelCombo;

        channelCounter = 0;
        for (i=0; true; i++) {
            channelCounter++;
            channelCombo = me.down('#mdc-step2-channel-combo-' + channelCounter);
            if (Ext.isEmpty(channelCombo)) {
                break;
            }
            if (previouslyMappedChannels) {
                channelCombo.setValue(previouslyMappedChannels[channelCounter]);
            } else if (channelCombo.getStore().getCount()===1) {
                channelCombo.setValue(channelCombo.getStore().getAt(0).get('id'));
            }
        }
    },

    createChannelStore: function(loadProfileConfigurationRecord, dataLoggerChannelRecords) {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Mdc.model.Channel',
                sorters: [{
                    property: 'extendedChannelName',
                    direction: 'ASC'
                }],
                autoLoad: false
            });

        Ext.Array.forEach(dataLoggerChannelRecords, function(channelRecord){
            if (loadProfileConfigurationRecord.get('timeDuration').asSeconds === channelRecord.interval.asSeconds) {
                store.add(channelRecord);
            }
        }, me);
        return store;
    }
});