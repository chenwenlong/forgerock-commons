/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2015 ForgeRock AS
 */
package org.forgerock.audit.event;

import static org.forgerock.json.fluent.JsonValue.*;

import java.util.Arrays;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.audit.event.AuditEvent;

/**
 * Root builder for all audit events.
 *
 * @param <T> the type of the builder
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AuditEventBuilder<T extends AuditEventBuilder<T>> {

    /** Represents the event as a JSON value. */
    protected final JsonValue jsonValue = json(object());

    /** A reference to this object with its actual type. */
    //private T self;

    /**
     * Creates the builder.
     */
    protected AuditEventBuilder() {
        //self = self();
    }

    /**
     * Returns this object, as its actual type.
     *
     * @return this object
     */
    protected abstract T self();

    /**
     * Generates the audit event.
     *
     * @return the audit event
     */
    public AuditEvent toEvent() {
        return new AuditEvent(jsonValue);
    }

    /**
     * Sets the provided time stamp for the event.
     *
     * @param t the time stamp.
     * @return this builder
     */
    public T timestamp(String t) {
        jsonValue.put("timestamp", t);
        return self();
    }

    /**
     * Sets the provided transactionId for the event.
     *
     * @param id the transaction id.
     * @return this builder
     */
    public T transactionId(String id) {
        jsonValue.put("transactionId", id);
        return self();
    }

    /**
     * Sets a field that is not predefined in the builder.
     *
     * @param name the name of the field.
     * @param value the value of the field
     * @return this builder
     */
    public T aField(String name, Object value) {
        jsonValue.put(name, value);
        return self();
    }

    /**
     * Builder for audit access events.
     * <p>
     * This builder should not be used directly but be specialized for each product to allow to define
     * new specific fields, e.g
     * <pre>
     * <code>
     * class MyProductAccessAuditEventBuilder{@code <T extends MyProductAccessAuditEventBuilder<T>>}
        extends AccessAuditEventBuilder{@code <T>} {
     *
     *    public static {@code <T>} OpenProductAccessAuditEventBuilder{@code <?>} productAccessEvent() {
     *       return new OpenProductAccessAuditEventBuilder();
     *    }
     *
     *    public T someField(String v) {
     *      jsonValue.put("someField", v);
     *      return self();
     *    }
     *
     *    {@literal @}Override
     *    protected T self() {
     *      return (T) this;
     *    }
     *
     *    ...
     * }
     * </code>
     * </pre>
     */
    public static class AccessAuditEventBuilder<T extends AccessAuditEventBuilder<T>> extends AuditEventBuilder<T> {

        protected AccessAuditEventBuilder() {
            // reduced visibility
            super();
        }

        /**
         * Starts to build an audit access event.
         * <p>
         * Note: it is preferable to use a specialized builder that allow to add
         * fields specific to a product.
         *
         * @return an audit access event builder
         */
        public static AccessAuditEventBuilder<?> accessEvent() {
            return new AccessAuditEventBuilder();
        }

        /**
         * Sets the provided message id for the event.
         *
         * @param id the message id.
         * @return this builder
         */
        public T messageId(String id) {
            jsonValue.put("messageId", id);
            return self();
        }

        /**
         * Sets the provided authentication id for the event.
         *
         * @param id the authentication id.
         * @return this builder
         */
        public T authenticationId(String id) {
            jsonValue.put("authenticationId", id);
            return self();
        }

        /**
         * Sets the provided server values for the event.
         *
         * @param ip the ip of the server.
         * @param port the port of the server.
         * @return this builder
         */
        public T server(String ip, String port) {
            JsonValue object = json(object(
                    field("ip", ip),
                    field("port", port)));
            jsonValue.put("server", object);
            return self();
        }

        /**
         * Sets the provided client ip and port for the event.
         *
         * @param ip the ip of the client.
         * @param port the port of the client.
         * @return this builder
         */
        public T client(String ip, String port) {
            JsonValue object = json(object(
                    field("ip", ip),
                    field("port", port)));
            jsonValue.put("client", object);
            return self();
        }

        /**
         * Sets the provided client hostname, ip and port for the event.
         *
         * @param host the hostname of the client.
         * @param ip the ip of the client.
         * @param port the port of the client.
         * @return this builder
         */
        public T client(String host, String ip, String port) {
            JsonValue object = json(object(
                    field("host", host),
                    field("ip", ip),
                    field("port", port)));
            jsonValue.put("client", object);
            return self();
        }

        /**
         * Sets the provided resource operation method and action for the event.
         *
         * @param method the method of the operation (for CREST, expect one method in CRUDPAQ).
         * @param action the detailed action of the operation.
         * @return this builder
         */
        public T resourceOperation(String method, String action) {
            JsonValue object = json(object(
                    field("method", method),
                    field("action", action)));
            jsonValue.put("resourceOperation", object);
            return self();
        }

        /**
         * Sets the provided resource operation method for the event.
         *
         * If the method is an Action, use {@code resourceOperation(String, String)} method instead.
         *
         * @param method the method of the operation (for CREST, expect one method in CRUDPQ).
         * @return this builder
         */
        public T resourceOperation(String method) {
            JsonValue object = json(object(field("method", method)));
            jsonValue.put("resourceOperation", object);
            return self();
        }

        /**
         * Sets the provided authorizationId fields for the event.
         *
         * @param component the component part of the authorization id.
         * @param id the id part of the authorization id.
         * @param roles the list of roles. Roles are optional.
         * @return this builder
         */
        public T authorizationId(String component, String id, String...roles) {
            JsonValue object = json(object(
                    field("component", component),
                    field("id", id)));
            if (roles != null && roles.length > 0) {
                Object roleList = json(array(Arrays.copyOf(roles, roles.length, Object[].class)));
                object.put("roles", roleList);
            }
            jsonValue.put("authorizationId", object);
            return self();
        }

        /**
         * Sets the provided HTTP fields for the event.
         *
         * @param method the HTTP method.
         * @param path the path of HTTP request.
         * @param queryString the query string of HTTP request.
         * @param headers the list of headers of HTTP request. The headers are optional.
         * @return this builder
         */
        public T http(String method, String path, String queryString, String...headers) {
            JsonValue object = json(object(
                    field("method", method),
                    field("path", path),
                    field("queryString", queryString)));
            if (headers != null && headers.length > 0) {
                Object headersList = json(array(Arrays.copyOf(headers, headers.length, Object[].class)));
                object.put("headers", headersList);
            }
            jsonValue.put("http", object);
            return self();
        }

        /**
         * Sets the provided response for the event.
         *
         * @param status the status of the operation.
         * @param elapsedTime the execution time of the action.
         * @return this builder
         */
        public T response(String status, String elapsedTime) {
            JsonValue object = json(object(
                    field("status", status),
                    field("elapsedTime", elapsedTime)));
            jsonValue.put("response", object);
            return self();
        }

        @Override
        protected T self() {
            return (T) this;
        }

    }

}