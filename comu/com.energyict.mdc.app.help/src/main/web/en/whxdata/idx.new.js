/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function () {
    var index = {"type": "index"};
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), index, {sync: true});
})();