/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.EstimationComment', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.estimation-comments',
    requires: [
        'Est.estimationrules.store.EstimationComment'
    ],

    fieldLabel: Uni.I18n.translate('copyFromReference.estimationComment', 'EST', 'Estimation comment'),
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
                    var keepComment = {
                        comment: Uni.I18n.translate('copyFromReference.keepComment', 'EST', 'Keep original comment'),
                        id: -1
                    };
                    this.getStore().add(keepComment);
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
            tooltip: Uni.I18n.translate('general.clear', 'EST', 'Clear'),
            handler: function () {
                this.previousSibling('#estimation-comment').reset();
            }

        }
    ]
});