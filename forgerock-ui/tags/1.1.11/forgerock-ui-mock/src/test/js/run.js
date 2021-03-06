/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global require, QUnit, localStorage */

require([
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/EventManager",
    "../test/tests/commons",
    "../test/tests/user",
    "../test/tests/mock",
    "../test/tests/getLoggedUser"
], function (constants, eventManager, commonsTests, userTests, mockTests, getLoggedUser) {

    $.doTimeout = function (name, time, func) {
        func(); // run the function immediately rather than delayed.
    }

    sinon.stub(eventManager, "sendEvent", function (eventId, event) {

        // the normal behavior for sendEvent:
        $(document).trigger(eventId, event);

        // special extended behavior for our stub:
        if (eventId === constants.EVENT_APP_INTIALIZED) {
            eventManager.sendEvent.restore();
            // delayed testing start gives the app time to stablize during startup
            _.delay(function () {

                var server = require("org/forgerock/mock/ui/common/main/MockServer").instance,
                    userParams = {
                        "username": "test",
                        "password": "test"
                    };

                QUnit.start();

                commonsTests.executeAll(server, userParams);
                userTests.executeAll(server, getLoggedUser());
                mockTests.executeAll(server, userParams);

                QUnit.done(function () {
                    localStorage.clear();
                    Backbone.history.stop();
                    window.location.hash = "";
                });
                
            }, 100);
        }
    });
});