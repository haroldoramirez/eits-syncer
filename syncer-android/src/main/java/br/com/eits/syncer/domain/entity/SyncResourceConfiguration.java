package br.com.eits.syncer.domain.entity;

import br.com.eits.syncer.application.restful.ISyncResource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Contract;
import feign.Feign;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public class SyncResourceConfiguration
{
    /**
     *
     */
    public static final String SERVICE_NAME_KEY = "serviceName";

    /**
     *
     */
    private Map<String, String> syncURLs = new HashMap<>();

    /**
     *
     */
    private RequestInterceptor requestInterceptor;

    /**
     *
     */
    private ObjectMapper objectMapper = new ObjectMapper();
    /**
     *
     */
    private Contract contract = new JAXRSContract();

    /**
     *
     */
    public SyncResourceConfiguration()
    {
        //configure the default objectMapper
        this.objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        this.objectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
        this.objectMapper.enableDefaultTypingAsProperty( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "@type" );
    }

    /**
     *
     * @param serviceName
     * @return
     */
    public ISyncResource getSyncResource( String serviceName )
    {
        //TODO Should we make a cache of syncResource instance?

        final String serviceUrl = this.syncURLs.get(serviceName);
        Objects.requireNonNull( serviceUrl, "An URL was not found to the service name: "+serviceName );

        final Feign.Builder builder = Feign.builder()
                .contract( this.contract )
                .encoder( new JacksonEncoder( this.objectMapper ) )
                .decoder( new JacksonDecoder( this.objectMapper ) );

        if ( this.requestInterceptor != null )
        {
            builder.requestInterceptor( this.requestInterceptor );
        }

        return builder.target( ISyncResource.class, serviceUrl );
    }

    /**
     *
     * @return default sync resource
     */
    public ISyncResource getSyncResource()
    {
        //get the first service name
        final String defaultServiceName = (String) this.getServiceNames().toArray()[0];
        return this.getSyncResource( defaultServiceName );
    }


        /**
         *
         * @param credentials
         */
    public void setBasicCredentials( String credentials )
    {
        if ( credentials != null && !credentials.isEmpty() && credentials.contains(":") )
        {
            final String username = credentials.split(":")[0];
            final String password = credentials.split(":")[1];

            this.requestInterceptor = new BasicAuthRequestInterceptor(username, password);
        }
        else
        {
            throw new IllegalArgumentException("The basic credentials must a meta-data like: " +
                    "        <meta-data android:name=\"sync-basic-credentials\"\n" +
                    "                   android:value=\"username:password\"/>");
        }
    }

    /**
     *
     * @param urls
     */
    public void setSyncURLs( String urls )
    {
        Objects.requireNonNull( urls, "The Sync URLs must be not null." );

        try
        {
            this.syncURLs = this.objectMapper.readValue( urls, Map.class );
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("The sync-urls meta-data must be set like: " +
                    "<meta-data android:name=\"sync-urls\"\n" +
                    "                   android:value=\"{'default':'http://host1.com', 'service2':'http://host2.com'}\"/>", e);
        }
    }

    /**
     *
     * @return
     */
    public Set<String> getServiceNames()
    {
        if ( syncURLs.isEmpty() ) throw new IllegalStateException("The Sync URLs is empty. Please verify you manifest.");
        return this.syncURLs.keySet();
    }

    /**
     *
     * @param requestInterceptor
     */
    public void setRequestInterceptor(RequestInterceptor requestInterceptor)
    {
        this.requestInterceptor = requestInterceptor;
    }

    /**
     *
     * @param objectMapper
     */
    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    /**
     *
     * @return
     */
    public ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    /**
     *
     * @param contract
     */
    public void setContract(Contract contract)
    {
        Objects.requireNonNull( contract, "The feign contract must be not null." );
        this.contract = contract;
    }
}