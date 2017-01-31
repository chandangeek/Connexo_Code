/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalltypes-preview-form',
    requires: [
        'Uni.util.FormEmptyMessage'
    ],
    defaults: {
        labelWidth: 250
    },
    layout: {
        type: 'vbox'
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                itemId: 'sct-top-container',
                layout: {
                    type: 'vbox'
                }
            },
            {
                xtype: 'container',
                itemId: 'sct-bottom-container',
                width: '100%',
                layout: {
                    type: 'column'
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'sct-left-container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox'
                        }
                    },
                    {
                        xtype: 'container',
                        itemId: 'sct-right-container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    updatePreview: function (record) {
        var me = this,
            cps,
            topContainer = me.down('#sct-top-container'),
            wrapContainer1 = me.down('#sct-left-container'),
            wrapContainer2 = me.down('#sct-right-container'),
            activeSetsToProcess = false,
            i;

        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        topContainer.removeAll();
        wrapContainer1.removeAll();
        wrapContainer2.removeAll();

        cps = record.get('customPropertySets');

        if(cps) {
            if(cps.length > 0) {
                // First process the inactive ones
                for(i = 0; i < cps.length; i++) {
                    if (!cps[i].active) {
                        me.addInfoMessage(
                            Uni.I18n.translate('servicecalltype.setXUnavailable', 'SCT', 'Custom attribute set "{0}" is (temporarily) unavailable.', cps[i].name),
                            topContainer
                        );
                    } else {
                        activeSetsToProcess = true;
                    }
                }

                // Then process the active ones
                if (activeSetsToProcess) {
                    for (i = 0; i < cps.length; i++) {
                        if (cps[i].active) {
                            me.addInfoToPreview(cps[i], i % 2 === 0 ? wrapContainer1 : wrapContainer2);
                        }
                    }
                    wrapContainer1.show();
                    wrapContainer2.show();
                } else {
                    wrapContainer1.hide();
                    wrapContainer2.hide();
                }
            } else {
                me.addInfoMessage(
                    Uni.I18n.translate('servicecalltype.noCASmessage', 'SCT', 'This service call type has no attributes that need to be defined.'),
                    topContainer
                );
                wrapContainer1.hide();
                wrapContainer2.hide();
            }
        }

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    addInfoMessage: function(message, container) {
        container.add(
            Ext.create('Uni.util.FormEmptyMessage', { text: message })
        );
    },

    addInfoToPreview: function (customPropertySetInfo, wrapContainer) {
        var container,
            attributesContainer;

        container = Ext.create('Ext.form.FieldContainer', {
            labelAlign: 'top',
            fieldLabel: customPropertySetInfo.name
        });

        attributesContainer = Ext.create('Ext.form.FieldContainer', {
            fieldLabel: Uni.I18n.translate('general.attributes', 'SCT', 'Attributes'),
            layout: {
                type: 'vbox'
            },
            labelWidth: 250
        });

        customPropertySetInfo.attributes.forEach(function (attribute) {
            attributesContainer.add(
                Ext.create('Ext.form.field.Display', {
                    value: attribute.name,
                    labelWidth: 250
                }))
        });
        container.add(attributesContainer);
        wrapContainer.add(container);
    }
});
