/*
 * uIAM - the IAM for microservices
 * Copyright (C) SoftInstigate Srl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.uiam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.uiam.utils.URLUtils;

/**
 * Utility class to help dealing with the uIAM configuration file.
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class Configuration {
    /**
     * the uIAM version It is read from the JAR's MANIFEST.MF file, which is
     * automatically generated by the Maven build process
     */
    public static final String UIAM_VERSION = Configuration.class.getPackage().getImplementationVersion() == null
            ? "unknown, not packaged"
            : Configuration.class.getPackage().getImplementationVersion();

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public static final String DEFAULT_ROUTE = "0.0.0.0";

    /**
     * default ajp host 0.0.0.0.
     */
    public static final String DEFAULT_AJP_HOST = DEFAULT_ROUTE;

    /**
     * default ajp port 8009.
     */
    public static final int DEFAULT_AJP_PORT = 8009;

    /**
     * default http host 0.0.0.0.
     */
    public static final String DEFAULT_HTTP_HOST = DEFAULT_ROUTE;

    /**
     * default http port 8080.
     */
    public static final int DEFAULT_HTTP_PORT = 8080;

    /**
     * default https host 0.0.0.0.
     */
    public static final String DEFAULT_HTTPS_HOST = DEFAULT_ROUTE;

    /**
     * default https port 4443.
     */
    public static final int DEFAULT_HTTPS_PORT = 4443;

    /**
     * default uIAM instance name default
     */
    public static final String DEFAULT_INSTANCE_NAME = "default";

    /**
     * the key for the services property.
     */
    public static final String SERVICES_KEY = "services";

    /**
     * the key for the args property.
     */
    public static final String ARGS_KEY = "args";

    /**
     * the key for the name property.
     */
    public static final String NAME_KEY = "name";

    /**
     * the key for the uri property.
     */
    public static final String SERVICE_URI_KEY = "uri";

    /**
     * the key for the secured property.
     */
    public static final String SERVICE_SECURED_KEY = "secured";

    /**
     * the key for the local-cache-enabled property.
     */
    public static final String LOCAL_CACHE_ENABLED_KEY = "local-cache-enabled";

    /**
     * the key for the force-gzip-encoding property.
     */
    public static final String FORCE_GZIP_ENCODING_KEY = "force-gzip-encoding";

    /**
     * the key for the direct-buffers property.
     */
    public static final String DIRECT_BUFFERS_KEY = "direct-buffers";

    /**
     * the key for the buffer-size property.
     */
    public static final String BUFFER_SIZE_KEY = "buffer-size";

    /**
     * the key for the worker-threads property.
     */
    public static final String WORKER_THREADS_KEY = "worker-threads";

    /**
     * the key for the io-threads property.
     */
    public static final String IO_THREADS_KEY = "io-threads";

    /**
     * the key for the requests-limit property.
     */
    public static final String REQUESTS_LIMIT_KEY = "requests-limit";

    /**
     * the key for the enable-log-file property.
     */
    public static final String ENABLE_LOG_FILE_KEY = "enable-log-file";

    /**
     * the key for the enable-log-console property.
     */
    public static final String ENABLE_LOG_CONSOLE_KEY = "enable-log-console";

    /**
     * the key for the log-level property.
     */
    public static final String LOG_LEVEL_KEY = "log-level";

    /**
     * the key for the log-file-path property.
     */
    public static final String LOG_FILE_PATH_KEY = "log-file-path";

    /**
     * the key for the class property.
     */
    public static final String CLASS_KEY = "class";

    /**
     * the key for the access-manager property.
     */
    public static final String ACCESS_MANAGER_KEY = "access-manager";

    /**
     * the key for the idms property.
     */
    public static final String IDMS_KEY = "idms";

    /**
     * the key for the auth Mechanism.
     */
    public static final String AUTH_MECHANISMS_KEY = "auth-mechanisms";

    /**
     * the key for the proxies property.
     */
    public static final String PROXY_KEY = "proxies";

    /**
     * the key for the internal-uri property.
     */
    public static final String PROXY_URI_KEY = "internal-uri";

    /**
     * the key for the external-url property.
     */
    public static final String PROXY_URL_KEY = "external-url";

    /**
     * the key for the connections-per-thread property.
     */
    public static final String PROXY_CONNECTIONS_PER_THREAD = "connections-per-thread";

    /**
     * the key for the max-queue-size property.
     */
    public static final String PROXY_MAX_QUEUE_SIZE = "max-queue-size";

    /**
     * the key for the soft-max-connections-per-thread property.
     */
    public static final String PROXY_SOFT_MAX_CONNECTIONS_PER_THREAD = "soft-max-connections-per-thread";

    /**
     * the key for the connections-ttl property.
     */
    public static final String PROXY_TTL = "connections-ttl";


    /**
     * the key for the problem-server-retry property.
     */
    public static final String PROXY_PROBLEM_SERVER_RETRY = "problem-server-retry";

    /**
     * the key for the auth-db property.
     */
    public static final String MONGO_AUTH_DB_KEY = "auth-db";

    /**
     * the key for the named-singletons property.
     */
    public static final String NAMED_SINGLETONS_KEY = "named-singletons";

    /**
     * the key for the certpassword property.
     */
    public static final String CERT_PASSWORD_KEY = "certpassword";

    /**
     * the key for the keystore-password property.
     */
    public static final String KEYSTORE_PASSWORD_KEY = "keystore-password";

    /**
     * the key for the keystore-file property.
     */
    public static final String KEYSTORE_FILE_KEY = "keystore-file";

    /**
     * the key for the use-embedded-keystore property.
     */
    public static final String USE_EMBEDDED_KEYSTORE_KEY = "use-embedded-keystore";

    /**
     * the key for the ajp-host property.
     */
    public static final String AJP_HOST_KEY = "ajp-host";

    /**
     * the key for the ajp-port property.
     */
    public static final String AJP_PORT_KEY = "ajp-port";

    /**
     * the key for the ajp-listener property.
     */
    public static final String AJP_LISTENER_KEY = "ajp-listener";

    /**
     * the key for the http-host property.
     */
    public static final String HTTP_HOST_KEY = "http-host";

    /**
     * the key for the http-port property.
     */
    public static final String HTTP_PORT_KEY = "http-port";

    /**
     * the key for http-listener the property.
     */
    public static final String HTTP_LISTENER_KEY = "http-listener";

    /**
     * the key for the https-host property.
     */
    private static final String HTTPS_HOST_KEY = "https-host";

    /**
     * the key for the https-port property.
     */
    private static final String HTTPS_PORT_KEY = "https-port";

    /**
     * the key for the https-listener property.
     */
    public static final String HTTPS_LISTENER = "https-listener";

    /**
     * the key for the instance-name property.
     */
    public static final String INSTANCE_NAME_KEY = "instance-name";

    /**
     * the key for the tokenManager property.
     */
    public static final String AUTH_TOKEN = "token-manager";

    /**
     * Force http requests logging even if DEBUG is not set
     */
    public static final String LOG_REQUESTS_LEVEL_KEY = "requests-log-level";

    /**
     * The key for enabling the Ansi console (for logging with colors)
     */
    public static final String ANSI_CONSOLE_KEY = "ansi-console";

    /**
     * The key for specifying an initializer class
     */
    public static final String INITIALIZER_CLASS_KEY = "initializer-class";

    /**
     * The key to allow unescaped chars in URL
     */
    public static final String ALLOW_UNESCAPED_CHARACTERS_IN_URL = "allow-unescaped-characters-in-url";

    /**
     * undertow connetction options
     *
     * @see http://undertow.io/undertow-docs/undertow-docs-1.3.0/index.html#common-listener-optionshttp://undertow.io/undertow-docs/undertow-docs-1.3.0/index.html#common-listener-options
     */
    public static final String CONNECTION_OPTIONS_KEY = "connection-options";

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getConfigurationFromFile(final Path confFilePath) throws ConfigurationException {
        Yaml yaml = new Yaml();

        Map<String, Object> conf = null;

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(confFilePath.toFile());
            conf = (Map<String, Object>) yaml.load(fis);
        } catch (FileNotFoundException fne) {
            throw new ConfigurationException("configuration file not found", fne);
        } catch (Throwable t) {
            throw new ConfigurationException("error parsing the configuration file", t);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    LOGGER.warn("Can't close the FileInputStream", ioe);
                }
            }
        }

        return conf;
    }

    /**
     *
     * @param integers
     * @return
     */
    public static int[] convertListToIntArray(List<Object> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Object> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            Object o = iterator.next();

            if (o instanceof Integer) {
                ret[i] = (Integer) o;
            } else {
                return new int[0];
            }
        }

        return ret;
    }

    private boolean silent = false;
    private final boolean httpsListener;
    private final int httpsPort;
    private final String httpsHost;
    private final boolean httpListener;
    private final int httpPort;
    private final String httpHost;
    private final boolean ajpListener;
    private final int ajpPort;
    private final String ajpHost;
    private final String instanceName;
    private final boolean useEmbeddedKeystore;
    private final String keystoreFile;
    private final String keystorePassword;
    private final String certPassword;
    private final List<Map<String, Object>> proxies;
    private final List<Map<String, Object>> services;
    private final List<Map<String, Object>> authMechanisms;
    private final List<Map<String, Object>> idms;
    private final Map<String, Object> accessManager;
    private final Map<String, Object> tokenManager;
    private final String logFilePath;
    private final Level logLevel;
    private final boolean logToConsole;
    private final boolean logToFile;
    private final int requestsLimit;
    private final int ioThreads;
    private final int workerThreads;
    private final int bufferSize;
    private final boolean directBuffers;
    private final boolean forceGzipEncoding;
    private final Map<String, Object> connectionOptions;
    private final Integer logExchangeDump;
    private final boolean ansiConsole;
    private final String initializerClass;
    private final boolean allowUnescapedCharactersInUrl;

    /**
     * the configuration map
     */
    private final Map<String, Object> configurationFileMap;

    /**
     * Creates a new instance of Configuration with defaults values.
     */
    public Configuration() {
        this.configurationFileMap = null;

        ansiConsole = true;

        httpsListener = true;
        httpsPort = DEFAULT_HTTPS_PORT;
        httpsHost = DEFAULT_HTTPS_HOST;

        httpListener = true;
        httpPort = DEFAULT_HTTP_PORT;
        httpHost = DEFAULT_HTTP_HOST;

        ajpListener = false;
        ajpPort = DEFAULT_AJP_PORT;
        ajpHost = DEFAULT_AJP_HOST;

        instanceName = DEFAULT_INSTANCE_NAME;

        useEmbeddedKeystore = true;
        keystoreFile = null;
        keystorePassword = null;
        certPassword = null;

        proxies = new ArrayList<>();

        services = new ArrayList<>();

        authMechanisms = new ArrayList<>();

        idms = new ArrayList<>();

        accessManager = null;

        tokenManager = new HashMap<>();

        logFilePath = URLUtils.removeTrailingSlashes(System.getProperty("java.io.tmpdir"))
                .concat(File.separator + "uiam.log");

        logToConsole = true;
        logToFile = true;
        logLevel = Level.INFO;

        requestsLimit = 100;

        ioThreads = 2;
        workerThreads = 32;
        bufferSize = 16384;
        directBuffers = true;

        forceGzipEncoding = false;

        logExchangeDump = 0;

        connectionOptions = Maps.newHashMap();
        initializerClass = null;

        allowUnescapedCharactersInUrl = true;
    }

    /**
     * Creates a new instance of Configuration from the configuration file For any
     * missing property the default value is used.
     *
     * @param confFilePath the path of the configuration file
     * @throws io.uiam.ConfigurationException
     */
    public Configuration(final Path confFilePath) throws ConfigurationException {
        this(confFilePath, false);
    }

    /**
     * Creates a new instance of Configuration from the configuration file For any
     * missing property the default value is used.
     *
     * @param confFilePath the path of the configuration file
     * @param silent
     * @throws io.uiam.ConfigurationException
     */
    public Configuration(final Path confFilePath, boolean silent) throws ConfigurationException {
        this(getConfigurationFromFile(confFilePath), silent);
    }

    /**
     * Creates a new instance of Configuration from the configuration file For any
     * missing property the default value is used.
     *
     * @param conf   the key-value configuration map
     * @param silent
     * @throws io.uiam.ConfigurationException
     */
    public Configuration(Map<String, Object> conf, boolean silent) throws ConfigurationException {
        this.configurationFileMap = conf;

        this.silent = silent;

        ansiConsole = getOrDefault(conf, ANSI_CONSOLE_KEY, true);

        httpsListener = getOrDefault(conf, HTTPS_LISTENER, true);
        httpsPort = getOrDefault(conf, HTTPS_PORT_KEY, DEFAULT_HTTPS_PORT);
        httpsHost = getOrDefault(conf, HTTPS_HOST_KEY, DEFAULT_HTTPS_HOST);

        httpListener = getOrDefault(conf, HTTP_LISTENER_KEY, false);
        httpPort = getOrDefault(conf, HTTP_PORT_KEY, DEFAULT_HTTP_PORT);
        httpHost = getOrDefault(conf, HTTP_HOST_KEY, DEFAULT_HTTP_HOST);

        ajpListener = getOrDefault(conf, AJP_LISTENER_KEY, false);
        ajpPort = getOrDefault(conf, AJP_PORT_KEY, DEFAULT_AJP_PORT);
        ajpHost = getOrDefault(conf, AJP_HOST_KEY, DEFAULT_AJP_HOST);

        instanceName = getOrDefault(conf, INSTANCE_NAME_KEY, DEFAULT_INSTANCE_NAME);

        useEmbeddedKeystore = getOrDefault(conf, USE_EMBEDDED_KEYSTORE_KEY, true);
        keystoreFile = getOrDefault(conf, KEYSTORE_FILE_KEY, null);
        keystorePassword = getOrDefault(conf, KEYSTORE_PASSWORD_KEY, null);
        certPassword = getOrDefault(conf, CERT_PASSWORD_KEY, null);

        proxies = getAsListOfMaps(conf, PROXY_KEY, new ArrayList<>());

        services = getAsListOfMaps(conf, SERVICES_KEY, new ArrayList<>());

        authMechanisms = getAsListOfMaps(conf, AUTH_MECHANISMS_KEY, new ArrayList<>());

        idms = getAsListOfMaps(conf, IDMS_KEY, new ArrayList<>());

        accessManager = getAsMap(conf, ACCESS_MANAGER_KEY);

        tokenManager  = getAsMap(conf, AUTH_TOKEN);

        logFilePath = getOrDefault(conf, LOG_FILE_PATH_KEY, URLUtils
                .removeTrailingSlashes(System.getProperty("java.io.tmpdir")).concat(File.separator + "uiam.log"));
        String _logLevel = getOrDefault(conf, LOG_LEVEL_KEY, "INFO");
        logToConsole = getOrDefault(conf, ENABLE_LOG_CONSOLE_KEY, true);
        logToFile = getOrDefault(conf, ENABLE_LOG_FILE_KEY, true);

        Level level;

        try {
            level = Level.valueOf(_logLevel);
        } catch (Exception e) {
            if (!silent) {
                LOGGER.info("wrong value for parameter {}: {}. using its default value {}", "log-level", _logLevel,
                        "INFO");
            }
            level = Level.INFO;
        }

        logLevel = level;

        requestsLimit = getOrDefault(conf, REQUESTS_LIMIT_KEY, 100);

        ioThreads = getOrDefault(conf, IO_THREADS_KEY, 2);
        workerThreads = getOrDefault(conf, WORKER_THREADS_KEY, 32);
        bufferSize = getOrDefault(conf, BUFFER_SIZE_KEY, 16384);
        directBuffers = getOrDefault(conf, DIRECT_BUFFERS_KEY, true);

        forceGzipEncoding = getOrDefault(conf, FORCE_GZIP_ENCODING_KEY, false);

        logExchangeDump = getOrDefault(conf, LOG_REQUESTS_LEVEL_KEY, 0);

        connectionOptions = getAsMap(conf, CONNECTION_OPTIONS_KEY);

        initializerClass = getOrDefault(conf, INITIALIZER_CLASS_KEY, null);

        allowUnescapedCharactersInUrl = getOrDefault(conf, ALLOW_UNESCAPED_CHARACTERS_IN_URL, true);
    }

    @Override
    public String toString() {
        return "Configuration{" + "silent=" + silent + ", httpsListener=" + httpsListener + ", httpsPort=" + httpsPort
                + ", httpsHost=" + httpsHost + ", httpListener=" + httpListener + ", httpPort=" + httpPort
                + ", httpHost=" + httpHost + ", ajpListener=" + ajpListener + ", ajpPort=" + ajpPort + ", ajpHost="
                + ajpHost + ", instanceName=" + instanceName + ", useEmbeddedKeystore=" + useEmbeddedKeystore
                + ", keystoreFile=" + keystoreFile + ", keystorePassword=" + keystorePassword + ", certPassword="
                + certPassword + ", proxies=" + proxies + ", services=" + services + ", authMechanisms="
                + authMechanisms + ", idms=" + idms + ", accessManager=" + getAccessManager() + ", logFilePath="
                + logFilePath + ", logLevel=" + logLevel + ", logToConsole=" + logToConsole + ", logToFile=" + logToFile
                + ", requestsLimit=" + requestsLimit + ", ioThreads=" + ioThreads + ", workerThreads=" + workerThreads
                + ", bufferSize=" + bufferSize + ", directBuffers=" + directBuffers + ", forceGzipEncoding="
                + forceGzipEncoding + ", authToken=" + tokenManager
                + ", connectionOptions=" + connectionOptions + ", logExchangeDump=" + logExchangeDump + ", ansiConsole="
                + ansiConsole + ", initializerClass=" + initializerClass + ", cursorBatchSize="
                + allowUnescapedCharactersInUrl + ", configurationFileMap=" + configurationFileMap + '}';
    }

    /**
     * @return the proxies
     */
    public List<Map<String, Object>> getProxies() {
        return proxies;
    }

    /**
     *
     * @return true if the Ansi console is enabled
     */
    public boolean isAnsiConsole() {
        return ansiConsole;
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAsListOfMaps(final Map<String, Object> conf, final String key,
            final List<Map<String, Object>> defaultValue) {
        if (conf == null) {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file. using its default value {}",
                        key, defaultValue);
            }

            return defaultValue;
        }

        Object o = conf.get(key);

        if (o instanceof List) {
            return (List<Map<String, Object>>) o;
        } else {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file, using its default value {}",
                        key, defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAsMap(final Map<String, Object> conf, final String key) {
        if (conf == null) {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file.", key);
            }
            return null;
        }

        Object o = conf.get(key);

        if (o instanceof Map) {
            return (Map<String, Object>) o;
        } else {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file.", key);
            }
            return null;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @param silent
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <V extends Object> V getOrDefault(final Map<String, Object> conf, final String key, final V defaultValue, boolean silent) {
        if (conf == null || conf.get(key) == null) {
            // if default value is null there is no default value actually
            if (defaultValue != null && !silent) {
                LOGGER.debug("parameter {} not specified in the configuration file. using its default value {}", key,
                        defaultValue);
            }
            return defaultValue;
        }

        try {
            if (!silent) {
                LOGGER.debug("paramenter {} set to {}", key, conf.get(key));
            }
            return (V) conf.get(key);
        } catch (ClassCastException cce) {
            if (!silent) {
                LOGGER.warn("wrong value for parameter {}: {}. using its default value {}", key, conf.get(key),
                        defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @return
     */
    private <V extends Object> V getOrDefault(final Map<String, Object> conf, final String key, final V defaultValue) {
        return getOrDefault(conf, key, defaultValue, this.silent);
    }

    /**
     * @return the httpsListener
     */
    public boolean isHttpsListener() {
        return httpsListener;
    }

    /**
     * @return the httpsPort
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * @return the httpsHost
     */
    public String getHttpsHost() {
        return httpsHost;
    }

    /**
     * @return the httpListener
     */
    public boolean isHttpListener() {
        return httpListener;
    }

    /**
     * @return the httpPort
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * @return the httpHost
     */
    public String getHttpHost() {
        return httpHost;
    }

    /**
     * @return the ajpListener
     */
    public boolean isAjpListener() {
        return ajpListener;
    }

    /**
     * @return the ajpPort
     */
    public int getAjpPort() {
        return ajpPort;
    }

    /**
     * @return the ajpHost
     */
    public String getAjpHost() {
        return ajpHost;
    }

    /**
     * @return the useEmbeddedKeystore
     */
    public boolean isUseEmbeddedKeystore() {
        return useEmbeddedKeystore;
    }

    /**
     * @return the keystoreFile
     */
    public String getKeystoreFile() {
        return keystoreFile;
    }

    /**
     * @return the keystorePassword
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * @return the certPassword
     */
    public String getCertPassword() {
        return certPassword;
    }

    /**
     * @return the logFilePath
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * @return the logLevel
     */
    public Level getLogLevel() {

        String logbackConfigurationFile = System.getProperty("logback.configurationFile");
        if (logbackConfigurationFile != null && !logbackConfigurationFile.isEmpty()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger("io.uiam");
            return logger.getLevel();
        }

        return logLevel;
    }

    /**
     * @return the logToConsole
     */
    public boolean isLogToConsole() {
        return logToConsole;
    }

    /**
     * @return the logToFile
     */
    public boolean isLogToFile() {
        return logToFile;
    }

    /**
     * @return the ioThreads
     */
    public int getIoThreads() {
        return ioThreads;
    }

    /**
     * @return the workerThreads
     */
    public int getWorkerThreads() {
        return workerThreads;
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @return the directBuffers
     */
    public boolean isDirectBuffers() {
        return directBuffers;
    }

    /**
     * @return the forceGzipEncoding
     */
    public boolean isForceGzipEncoding() {
        return forceGzipEncoding;
    }

    /**
     * @return the authMechanisms
     */
    public List<Map<String, Object>> getAuthMechanisms() {
        return authMechanisms;
    }

    /**
     * @return the idms
     */
    public List<Map<String, Object>> getIdms() {
        return idms;
    }

    /**
     * @return the amClass
     */
    public Map<String, Object> getAm() {
        return getAccessManager();
    }

    /**
     * @return the requestsLimit
     */
    public int getRequestsLimit() {
        return requestsLimit;
    }

    /**
     * @return the services
     */
    public List<Map<String, Object>> getServices() {
        return Collections.unmodifiableList(services);
    }

    /**
     * @return the authToken
     */
    public Map<String, Object> getTokenManager() {
        return tokenManager;
    }

    /**
     *
     * @return the logExchangeDump Boolean
     */
    public Integer logExchangeDump() {
        return logExchangeDump;
    }

    /**
     * @return the connectionOptions
     */
    public Map<String, Object> getConnectionOptions() {
        return Collections.unmodifiableMap(connectionOptions);
    }

    /**
     * @return the instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * @return the configurationFileMap
     */
    public Map<String, Object> getConfigurationFileMap() {
        return Collections.unmodifiableMap(configurationFileMap);
    }

    /**
     * @return the initializerClass
     */
    public String getInitializerClass() {
        return initializerClass;
    }

    public boolean isAllowUnescapedCharactersInUrl() {
        return allowUnescapedCharactersInUrl;
    }

    /**
     * @return the accessManager
     */
    public Map<String, Object> getAccessManager() {
        return accessManager;
    }

}
