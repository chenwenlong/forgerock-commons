/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.caf.authn.test.configuration;

import com.google.inject.Singleton;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Resource;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResultHandler;
import org.forgerock.json.resource.ServerContext;
import org.forgerock.json.resource.SingletonResourceProvider;
import org.forgerock.json.resource.UpdateRequest;

import static org.forgerock.json.fluent.JsonValue.field;
import static org.forgerock.json.fluent.JsonValue.json;
import static org.forgerock.json.fluent.JsonValue.object;

/**
 * <p>CREST resource responsible for exposing the module configuration of the JASPI runtime.</p>
 *
 * <p>The resource offers only two operations, read and update. Performing a read will simply read the JASPI runtime's
 * current module configuration and performing an update will update the runtime's module configuration which will be
 * used for every subsequent request.</p>
 *
 * @since 1.5.0
 */
@Singleton
public class ConfigurationResource implements SingletonResourceProvider {

    private JsonValue moduleConfiguration = json(object(field("serverAuthContext", object())));

    /**
     * Unsupported operation.
     *
     * @param context {@inheritDoc}
     * @param request {@inheritDoc}
     * @param handler {@inheritDoc}
     */
    @Override
    public void actionInstance(ServerContext context, ActionRequest request, ResultHandler<JsonValue> handler) {
        handler.handleError(ResourceException.getException(ResourceException.NOT_SUPPORTED));
    }

    /**
     * Unsupported operation.
     *
     * @param context {@inheritDoc}
     * @param request {@inheritDoc}
     * @param handler {@inheritDoc}
     */
    @Override
    public void patchInstance(ServerContext context, PatchRequest request, ResultHandler<Resource> handler) {
        handler.handleError(ResourceException.getException(ResourceException.NOT_SUPPORTED));
    }

    /**
     * Will perform a read of the runtime's module configuration.
     *
     * @param context {@inheritDoc}
     * @param request {@inheritDoc}
     * @param handler {@inheritDoc}
     */
    @Override
    public void readInstance(ServerContext context, ReadRequest request, ResultHandler<Resource> handler) {
        handler.handleResult(new Resource("ModuleConfiguration", moduleConfiguration.hashCode() + "",
                moduleConfiguration));
    }

    /**
     * Will perform an update of the runtime's module configuration.
     *
     * @param context {@inheritDoc}
     * @param request {@inheritDoc}
     * @param handler {@inheritDoc}
     */
    @Override
    public void updateInstance(ServerContext context, UpdateRequest request, ResultHandler<Resource> handler) {

        moduleConfiguration = request.getContent();

        handler.handleResult(new Resource("ModuleConfiguration", moduleConfiguration.hashCode() + "",
                moduleConfiguration));
    }
}