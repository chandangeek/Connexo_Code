/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.common.EstimationComment', {
    alias: 'widget.estimation-comments',
    extend: 'Ext.form.FieldContainer',

    requires: [
        'Uni.property.store.EstimationComment'
    ],
    fieldLabel: Uni.I18n.translate('copyFromReference.estimationComment', 'CFG', 'Estimation comment'),
    layout: 'hbox',
    flex: 1,
    items: [
        {
            xtype: 'combobox',
            itemId: 'estimation-comment',
            flex: 1,
            name: 'commentId',
            store: Ext.create('Uni.property.store.EstimationComment').load(),
            valueField: 'id',
            displayField: 'comment',
            queryMode: 'local',
            editable: false,
            listeners: {
                afterrender: function () {
                    this.resetButton = this.nextSibling('#estimation-comment-default-button');
                },
                change: function (combobox) {
                    if (combobox.getValue()) {
                        this.resetButton.setDisabled(false);
                    } else {
                        this.resetButton.setDisabled(true);
                    }
                }
            }
        },
        {
            xtype: 'uni-default-button',
            itemId: 'estimation-comment-default-button',
            hidden: false,
            disabled: true,
            tooltip: Uni.I18n.translate('general.clear', 'UNI', 'Clear'),
            handler: function () {
                this.previousSibling('#estimation-comment').reset();
            }

        }
    ]
});