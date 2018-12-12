/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.LevelMap', {
    singleton: true,

    levelMap: {
        "LEVEL_1": Uni.I18n.translate('customattributesets.levelone', 'UNI', 'Level 1'),
        "LEVEL_2": Uni.I18n.translate('customattributesets.leveltwo', 'UNI', 'Level 2'),
        "LEVEL_3": Uni.I18n.translate('customattributesets.levelthree', 'UNI', 'Level 3'),
        "LEVEL_4": Uni.I18n.translate('customattributesets.levelfour', 'UNI', 'Level 4')
    },

    getTranslation: function (level) {
        return this.levelMap[level];
    },

    getPrivilegesString: function(privilegesArr) {
        var string = '';

        Ext.each(privilegesArr, function(privilege, index) {
            if (index != 0 ) {
                string += ' - ';
            }
            string += Uni.util.LevelMap.getTranslation(privilege);
        });
        return string;
    }
});
