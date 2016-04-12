Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step2',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
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

    initialize: function(loadProfileConfigurationRecords, dataLoggerChannelRecords) {
        var me = this;
        if (me.rendered) {
            me.doInitialize(loadProfileConfigurationRecords, dataLoggerChannelRecords);
        } else {
            me.on('afterrender', function() {
                me.doInitialize(loadProfileConfigurationRecords, dataLoggerChannelRecords);
            }, me, {single:true});
        }
    },

    doInitialize: function(loadProfileConfigurationRecords, dataLoggerChannelRecords) {
        var me = this,
            loadProfileCounter = 0,
            channelCounter = 0,
            form;

        me.down('#mdc-dataloggerslave-link-wizard-step2-container').removeAll();
        Ext.Array.forEach(loadProfileConfigurationRecords, function(record) {
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

            Ext.Array.forEach(record.get('channels'), function(channel) {
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

                if (channel.useMultiplier) {
                    form.add(
                        {
                            xtype: 'numberfield',
                            minValue: 1,
                            maxValue: 2147483647,
                            fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                            value: 1,
                            maxWidth: 375
                        }
                    );
                } else {
                    form.add(
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                            value: Uni.I18n.translate('general.channelDoesntUseMultiplier', 'MDC', "Channel doesn't use multiplier")
                        }
                    );
                }
            }, me);

        }, me);
    },

    createChannelStore: function(loadProfileConfigurationRecord, dataLoggerChannelRecords) {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
                sorters: [{
                    property: 'name',
                    direction: 'ASC'
                }],
                autoLoad: false
            });

        Ext.Array.forEach(dataLoggerChannelRecords, function(channelRecord){
            if (loadProfileConfigurationRecord.get('timeDuration').asSeconds === channelRecord.get('interval').asSeconds) {
                store.add(channelRecord);
            }
        }, me);

        return store;
    }
});