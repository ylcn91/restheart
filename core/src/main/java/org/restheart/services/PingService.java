/*-
 * ========================LICENSE_START=================================
 * restheart-security
 * %%
 * Copyright (C) 2018 - 2022 SoftInstigate
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
package org.restheart.services;

import java.util.Map;
import org.restheart.exchange.ByteArrayRequest;
import org.restheart.exchange.ByteArrayResponse;
import org.restheart.plugins.ByteArrayService;
import org.restheart.plugins.Inject;
import org.restheart.plugins.InjectConfiguration;
import org.restheart.plugins.RegisterPlugin;
import org.restheart.utils.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
@RegisterPlugin(name = "ping",
    description = "simple ping service",
    secure = false,
    enabledByDefault = true,
    defaultURI = "/ping",
    blocking = false)
public class PingService implements ByteArrayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private String msg = null;

    @Inject
    //TODO remove this
    public void testInject(String s) {
        LOGGER.debug("*************** wow we got: {} via @Inject", s);
    }

    @InjectConfiguration
    public void init(Map<String, Object> args) {
        this.msg = argOrDefault(args, "msg", "Greetings from RESTHeart!");
    }

    /**
     *
     */
    @Override
    public void handle(ByteArrayRequest request, ByteArrayResponse response) throws Exception {
        if (request.isGet()) {
            var accept = request.getHeader("Accept");

            if (accept != null && accept.startsWith("text/html")) {
                var content = "<div><h2>" + msg + "</h2></div>";
                response.setContent(content.getBytes());
                response.setContentType("text/html");
            } else {
                response.setContentType("text/plain");
                response.setContent(msg.getBytes());
            }
        } else if (request.isOptions()) {
            handleOptions(request);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }
    }
}
