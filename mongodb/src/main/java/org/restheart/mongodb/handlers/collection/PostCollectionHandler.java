/*-
 * ========================LICENSE_START=================================
 * restheart-mongodb
 * %%
 * Copyright (C) 2014 - 2022 SoftInstigate
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */
package org.restheart.mongodb.handlers.collection;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Optional;

import org.bson.BsonDocument;
import org.restheart.exchange.ExchangeKeys.DOC_ID_TYPE;
import org.restheart.exchange.MongoRequest;
import org.restheart.exchange.MongoResponse;
import org.restheart.handlers.PipelinedHandler;
import org.restheart.mongodb.db.Documents;
import org.restheart.mongodb.utils.ResponseHelper;
import org.restheart.mongodb.utils.MongoURLUtils;
import org.restheart.utils.HttpStatus;
import org.restheart.utils.RepresentationUtils;

/**
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class PostCollectionHandler extends PipelinedHandler {
    private final Documents documents = Documents.get();

    /**
     * Creates a new instance of PostCollectionHandler
     */
    public PostCollectionHandler() {
        this(null);
    }

    /**
     *
     * @param next
     */
    public PostCollectionHandler(PipelinedHandler next) {
        super(next);
    }


    /**
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var request = MongoRequest.of(exchange);
        var response = MongoResponse.of(exchange);

        if (request.isInError()) {
            next(exchange);
            return;
        }

        var _content = request.getContent();

        if (_content == null) {
            _content = new BsonDocument();
        }

        // cannot POST an array
        if (!_content.isDocument()) {
            response.setInError(HttpStatus.SC_NOT_ACCEPTABLE, "data must be a json object");
            next(exchange);
            return;
        }

        BsonDocument content = _content.asDocument();

        if (content.containsKey("_id") && content.get("_id").isString() && MongoRequest.isReservedDocumentId(request.getType(), content.get("_id"))) {
            response.setInError(HttpStatus.SC_FORBIDDEN, "reserved resource");
            next(exchange);
            return;
        }

        // if _id is not in content, it will be autogenerated as an ObjectId
        // check if the doc_type is different
        if (!content.containsKey("_id")) {
            if (!(request.getDocIdType() == DOC_ID_TYPE.OID) && !(request.getDocIdType() == DOC_ID_TYPE.STRING_OID)) {
                response.setInError(HttpStatus.SC_NOT_ACCEPTABLE, "_id in content body is mandatory for documents with id type " + request.getDocIdType().name());
                next(exchange);
                return;
            }
        }

        var result = documents.writeDocument(
            Optional.ofNullable(request.getClientSession()),
            request.rsOps(),
            request.getDBName(),
            request.getCollectionName(),
            request.getMethod(),
            request.getWriteMode(),
            Optional.ofNullable(content.get("_id")),
            Optional.ofNullable(request.getFiltersDocument()),
            Optional.ofNullable(request.getShardKey()),
            content,
            request.getETag(),
            request.isETagCheckRequired());

        response.setDbOperationResult(result);

        // inject the etag
        if (result.getEtag() != null) {
            ResponseHelper.injectEtagHeader(exchange, result.getEtag());
        }

        if (result.getHttpCode() == HttpStatus.SC_CONFLICT) {
            response.setInError(HttpStatus.SC_CONFLICT, "The document's ETag must be provided using the '" + Headers.IF_MATCH + "' header.");
            next(exchange);
            return;
        }

        // handle the case of duplicate key error
        if (result.getHttpCode() == HttpStatus.SC_EXPECTATION_FAILED) {
            response.setInError(HttpStatus.SC_EXPECTATION_FAILED, ResponseHelper.getMessageFromErrorCode(11000));
            next(exchange);
            return;
        }

        // handle the case of error result with exception
        if (result.getCause() != null) {
            response.setInError(result.getHttpCode(), result.getCause().getMessage());
            next(exchange);
            return;
        }

        response.setStatusCode(result.getHttpCode());

        // insert the Location handler for new documents
        // note, next handlers might change the status code
        if (result.getHttpCode() == HttpStatus.SC_CREATED) {
            response.getHeaders().add(HttpString.tryFromString("Location"), RepresentationUtils.getReferenceLink(MongoURLUtils.getRemappedRequestURL(exchange), result.getNewData().get("_id")));
        }

        next(exchange);
    }
}
