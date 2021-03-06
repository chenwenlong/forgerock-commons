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
 * Copyright 2012 ForgeRock AS.
 */
package org.forgerock.json.resource.servlet;

import static org.forgerock.json.resource.Resources.filterResource;
import static org.forgerock.json.resource.servlet.HttpUtils.HEADER_ETAG;
import static org.forgerock.json.resource.servlet.HttpUtils.HEADER_LOCATION;
import static org.forgerock.json.resource.servlet.HttpUtils.adapt;
import static org.forgerock.json.resource.servlet.HttpUtils.closeQuietly;
import static org.forgerock.json.resource.servlet.HttpUtils.getIfNoneMatch;
import static org.forgerock.json.resource.servlet.HttpUtils.getJsonGenerator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerator;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.Context;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResult;
import org.forgerock.json.resource.QueryResultHandler;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Request;
import org.forgerock.json.resource.RequestVisitor;
import org.forgerock.json.resource.Resource;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResultHandler;
import org.forgerock.json.resource.UpdateRequest;

/**
 * Common request processing used by {@code RequestDispatcher}s.
 */
abstract class RequestRunner implements ResultHandler<Connection>, RequestVisitor<Void, Void> {

    // Connection set on handleResult(Connection).
    private Connection connection = null;

    private final Context context;
    private final HttpServletRequest httpRequest;
    private final HttpServletResponse httpResponse;
    private final Request request;
    private final JsonGenerator writer;

    RequestRunner(final Context context, final Request request,
            final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
            throws Exception {
        this.context = context;
        this.request = request;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.writer = getJsonGenerator(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handleError(final ResourceException error) {
        doError(error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handleResult(final Connection result) {
        connection = result;

        // Dispatch request using visitor.
        request.accept(this, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitActionRequest(final Void p, final ActionRequest request) {
        connection.actionAsync(context, request, new ResultHandler<JsonValue>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleError(final ResourceException error) {
                doError(error);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleResult(final JsonValue result) {
                try {
                    if (result != null) {
                        writeJsonValue(result);
                    } else {
                        // No content.
                        httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    doComplete();
                } catch (final Exception e) {
                    doError(e);
                }
            }
        });
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitCreateRequest(final Void p, final CreateRequest request) {
        connection.createAsync(context, request, new ResultHandler<Resource>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleError(final ResourceException error) {
                doError(error);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleResult(final Resource result) {
                try {
                    if (result.getId() != null) {
                        httpResponse.setHeader(HEADER_LOCATION, getResourceURL(request, result));
                    }
                    httpResponse.setStatus(HttpServletResponse.SC_CREATED);
                    writeResource(result);
                    doComplete();
                } catch (final Exception e) {
                    doError(e);
                }
            }
        });
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitDeleteRequest(final Void p, final DeleteRequest request) {
        connection.deleteAsync(context, request, newResourceResultHandler());
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitPatchRequest(final Void p, final PatchRequest request) {
        connection.patchAsync(context, request, newResourceResultHandler());
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitQueryRequest(final Void p, final QueryRequest request) {
        connection.queryAsync(context, request, new QueryResultHandler() {
            private boolean needsHeader = true;

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleError(final ResourceException error) {
                if (!needsHeader) {
                    // Partial results.
                    try {
                        writer.writeEndArray();
                        writer.writeObjectFieldStart("error");
                        writer.writeNumberField("code", error.getCode());
                        writer.writeStringField("message", error.getMessage());
                        writer.writeEndObject();
                        writer.writeEndObject();
                    } catch (final IOException e) {
                        // FIXME: can we do anything with this?
                    }
                }
                doError(error);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean handleResource(final Resource resource) {
                try {
                    writeHeader();
                    writeJsonValue(resource.getContent());
                    return true;
                } catch (final Exception e) {
                    handleError(adapt(e));
                    return false;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleResult(final QueryResult result) {
                try {
                    writeHeader();
                    writer.writeEndArray();
                    writer.writeStringField("pagedResultsCookie", result.getPagedResultsCookie());
                    writer.writeNumberField("remainingPagedResults", result
                            .getRemainingPagedResults());
                    writer.writeEndObject();
                    doComplete();
                } catch (final Exception e) {
                    doError(e);
                }
            }

            private void writeHeader() throws IOException {
                if (needsHeader) {
                    writer.writeStartObject();
                    writer.writeArrayFieldStart("result");
                    needsHeader = false;
                }
            }
        });
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitReadRequest(final Void p, final ReadRequest request) {
        connection.readAsync(context, request, newResourceResultHandler());
        return null; // return Void.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void visitUpdateRequest(final Void p, final UpdateRequest request) {
        connection.updateAsync(context, request, newResourceResultHandler());
        return null; // return Void.
    }

    /**
     * Performs post-completion processing such as completing the AsyncContext
     * (Servlet3) or a latch (Servlet2).
     */
    abstract void postComplete();

    /**
     * Performs post-error processing such as sending error (Servlet3) or a
     * latch (Servlet2).
     *
     * @param e
     *            The error that occurred.
     */
    abstract void postError(Exception e);

    private void doComplete() {
        try {
            closeQuietly(connection, writer);
        } finally {
            postComplete();
        }
    }

    private void doError(final Exception e) {
        try {
            // Don't close the JSON writer because the request will become
            // "completed" which then prevents us from sending an error.
            closeQuietly(connection);
        } finally {
            postError(e);
        }
    }

    private String getResourceURL(final CreateRequest request, final Resource resource) {
        final StringBuffer buffer = httpRequest.getRequestURL();

        // Strip out everything except the scheme and host.
        buffer.setLength(buffer.length() - httpRequest.getRequestURI().length());

        // Add back the context and servlet paths.
        buffer.append(httpRequest.getContextPath());
        buffer.append(httpRequest.getServletPath());

        // Add new resource name and resource ID.
        buffer.append(request.getResourceName());
        if (!request.getResourceName().endsWith("/")) {
            buffer.append('/');
        }
        buffer.append(resource.getId());

        return buffer.toString();
    }

    private ResultHandler<Resource> newResourceResultHandler() {
        return new ResultHandler<Resource>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleError(final ResourceException error) {
                doError(error);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleResult(final Resource result) {
                try {
                    // Don't return the resource if this is a read request and the
                    // If-None-Match header was specified.
                    if (request instanceof ReadRequest) {
                        final String rev = getIfNoneMatch(httpRequest);
                        if (rev != null && rev.equals(result.getRevision())) {
                            // No change so 304.
                            throw ResourceException.getException(304, "Not Modified", null, null);
                        }
                    }

                    writeResource(result);
                    doComplete();
                } catch (final Exception e) {
                    doError(e);
                }
            }
        };
    }

    private void writeJsonValue(JsonValue json) throws IOException {
        writer.writeObject(filterResource(json, request.getFieldFilters()).getObject());
    }

    private void writeResource(final Resource resource) throws IOException {
        if (resource.getRevision() != null) {
            final StringBuilder builder = new StringBuilder();
            builder.append('"');
            builder.append(resource.getRevision());
            builder.append('"');
            httpResponse.setHeader(HEADER_ETAG, builder.toString());
        }
        writeJsonValue(resource.getContent());
    }
}
