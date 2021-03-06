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
 * Copyright 2013-2014 ForgeRock AS.
 */
package org.forgerock.json.resource;

import java.util.Collection;

import org.forgerock.json.fluent.JsonValue;

/**
 * An abstract base class from which connection wrappers may be easily
 * implemented. The default implementation of each method is to delegate to the
 * wrapped connection.
 *
 * @param <C>
 *            The type of wrapped connection.
 */
public abstract class AbstractConnectionWrapper<C extends Connection> implements Connection {
    /**
     * The wrapped connection.
     */
    protected final C connection;

    /**
     * Creates a new connection wrapper.
     *
     * @param connection
     *            The connection to be wrapped.
     */
    protected AbstractConnectionWrapper(final C connection) {
        this.connection = connection;
    }

    /**
     * Optional Context-transformation function if the implementer has requirements to override
     * the {@link Context} provided in the {@link Connection}'s method invocations.
     * <p>
     * The default implementation is a pass-through no-op.
     *
     * @param context the request context
     * @return the transformed context
     */
    protected Context transform(Context context) {
        return context;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public JsonValue action(Context context, ActionRequest request) throws ResourceException {
        return connection.action(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<JsonValue> actionAsync(Context context, ActionRequest request,
            ResultHandler<? super JsonValue> handler) {
        return connection.actionAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public void close() {
        connection.close();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public Resource create(Context context, CreateRequest request) throws ResourceException {
        return connection.create(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<Resource> createAsync(Context context, CreateRequest request,
            ResultHandler<? super Resource> handler) {
        return connection.createAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public Resource delete(Context context, DeleteRequest request) throws ResourceException {
        return connection.delete(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<Resource> deleteAsync(Context context, DeleteRequest request,
            ResultHandler<? super Resource> handler) {
        return connection.deleteAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public boolean isClosed() {
        return connection.isClosed();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public boolean isValid() {
        return connection.isValid();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public Resource patch(Context context, PatchRequest request) throws ResourceException {
        return connection.patch(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<Resource> patchAsync(Context context, PatchRequest request,
            ResultHandler<? super Resource> handler) {
        return connection.patchAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public QueryResult query(Context context, QueryRequest request, QueryResultHandler handler)
            throws ResourceException {
        return connection.query(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public QueryResult query(Context context, QueryRequest request,
            Collection<? super Resource> results) throws ResourceException {
        return connection.query(transform(context), request, results);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<QueryResult> queryAsync(Context context, QueryRequest request,
            QueryResultHandler handler) {
        return connection.queryAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public Resource read(Context context, ReadRequest request) throws ResourceException {
        return connection.read(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<Resource> readAsync(Context context, ReadRequest request,
            ResultHandler<? super Resource> handler) {
        return connection.readAsync(transform(context), request, handler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public Resource update(Context context, UpdateRequest request) throws ResourceException {
        return connection.update(transform(context), request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation is to delegate.
     */
    public FutureResult<Resource> updateAsync(Context context, UpdateRequest request,
            ResultHandler<? super Resource> handler) {
        return connection.updateAsync(transform(context), request, handler);
    }

}
