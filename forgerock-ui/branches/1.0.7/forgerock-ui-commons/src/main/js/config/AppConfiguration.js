/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 ForgeRock AS. All rights reserved.
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

/*global define*/

/**
 * @author yaromin
 */
define("config/AppConfiguration", [
    "org/forgerock/commons/ui/common/util/Constants", 
    "org/forgerock/commons/ui/common/main/EventManager"
], function(constants, eventManager) {
    var obj = {
    moduleDefinition: [   
        {
            moduleClass: "org/forgerock/commons/ui/common/main/ProcessConfiguration",
            configuration: {
            processConfigurationFiles: [
                "config/process/CommonConfig"
            ]
            } 
        },
        {
            moduleClass: "org/forgerock/commons/ui/common/main/ErrorsHandler",
            configuration: {
                defaultHandlers: {
                },
                loader: [
                         {"defaultHandlers":"config/errorhandlers/CommonErrorHandlers"}
                ]
            } 
        },
        {
            moduleClass: "org/forgerock/commons/ui/common/components/Navigation",
            configuration: {
                links: {                    
                    "default" : {
                        "urls": {
                            "openidm": {
                                "url": "http://forgerock.com/openidm.html",
                                "name": "OpenIDM"
                            }
                        }    
                    }
                }                                       
            } 
        },
        {
            moduleClass: "org/forgerock/commons/ui/common/main/ValidatorsManager",
            configuration: {
                validators: {
                },
                loader: [
                         {"validators":"config/validators/CommonValidators"}
                ]
            } 
        }
        ],
        loggerLevel: 'debug'
    };
    return obj;
});
