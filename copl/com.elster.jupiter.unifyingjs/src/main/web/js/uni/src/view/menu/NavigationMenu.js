/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.menu.NavigationMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigation-menu',
    cls: 'x-navigation-menu',

    requires: [
        'Uni.view.menu.NavigationItem'
    ],

    defaults: {
        xtype: 'navigation-item'
    },

    floating: false,
    hidden: false,
    activeStep: 1,
    minWidth: 256,
    jumpBack: true,
    jumpForward: false,

    listeners: {
        add: function (menu, item, index) {
            item.renderData.index = item.index = ++index;
            this.updateItemCls(index, false)
        },
        click: function (menu, item) {
            item && item.index < menu.activeStep ?
                (menu.jumpBack ? menu.moveTo(item.index) : null) :
                (menu.jumpForward ? menu.moveTo(item.index) : null)
        }
    },

    updateItemCls: function (index, markInvalid) {
        var me = this,
            item = me.items.getAt(index - 1),
            classesToAdd;

        item.removeCls(['step-completed', 'step-active', 'step-non-completed', 'not-a-clickable', 'step-error']);

        if (index < me.activeStep) {
            classesToAdd = me.jumpBack ? 'step-completed' : ['step-completed', 'not-a-clickable'];
        } else if (index > me.activeStep) {
            classesToAdd = me.jumpForward ? 'step-non-completed' : ['step-non-completed', 'not-a-clickable'];
        } else if (markInvalid) {
            classesToAdd = ['step-error', 'not-a-clickable'];
        } else {
            classesToAdd = ['step-active', 'not-a-clickable'];
        }

        item.addCls(classesToAdd);
    },

    moveTo: function (step) {
        var me = this;
        me.moveToStep(step);
        me.fireEvent('movetostep', me.activeStep)
    },

    moveToStep: function (step) {
        var me = this,
            stepCount = me.items.getCount();
        if (1 < step < stepCount) {
            me.activeStep = step;
            me.items.each(function (item) {
                var index = item.index;
                me.updateItemCls(index, false);
            });
        }
    },

    markInvalid: function () {
        this.updateItemCls(this.getActiveStep(), true);
    },

    getActiveStep: function () {
        return this.activeStep;
    },

    moveNextStep: function () {
        this.moveToStep(this.activeStep + 1);
    },

    movePrevStep: function () {
        this.moveToStep(this.activeStep - 1);
    }
});