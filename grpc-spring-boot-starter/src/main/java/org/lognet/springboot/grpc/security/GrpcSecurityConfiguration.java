package org.lognet.springboot.grpc.security;

import io.grpc.ServerInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;

import java.util.Collection;

@Configuration
public class GrpcSecurityConfiguration {
    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ObjectPostProcessor<Object> objectObjectPostProcessor;

    @SuppressWarnings({ "rawtypes" })
    private Collection<GrpcSecurityConfigurer> grpcSecurityConfigurers;

    private GrpcSecurity grpcSecurity;



    @Bean
    @ConditionalOnMissingBean(GrpcSecurityConfigurerAdapter.class)
    public GrpcSecurityConfigurerAdapter defaultAdapter(){
        return  new GrpcSecurityConfigurerAdapter() {
        };
    }

    @Bean
    @GRpcGlobalInterceptor
    public ServerInterceptor springGrpcSecurityInterceptor() throws Exception   {
        boolean hasConfigurers = grpcSecurityConfigurers != null && !grpcSecurityConfigurers.isEmpty();
        if (!hasConfigurers) {
            GrpcSecurityConfigurerAdapter adapter = objectObjectPostProcessor.postProcess(new GrpcSecurityConfigurerAdapter() {
                    });
            grpcSecurity.apply(adapter);
        }
        return grpcSecurity.build();


    }

    @Bean
    public BasicAuthSchemeSelector basicAuthSchemeSelector() {
        return new BasicAuthSchemeSelector();
    }

    @Bean
    @ConditionalOnClass(name = {
            "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken",
            "org.springframework.security.oauth2.core.OAuth2AuthenticationException"})
    public BearerTokenAuthSchemeSelector bearerTokenAuthSchemeSelector() {
        return new BearerTokenAuthSchemeSelector();
    }

    @Autowired(required = false)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setFilterChainProxySecurityConfigurer(ObjectPostProcessor<Object> objectPostProcessor) throws Exception {

        grpcSecurity = objectPostProcessor.postProcess(new GrpcSecurity(objectPostProcessor));

        grpcSecurityConfigurers =  beanFactory.getBeansOfType(GrpcSecurityConfigurer.class).values();

        for(GrpcSecurityConfigurer configurer: grpcSecurityConfigurers){
            grpcSecurity.apply(configurer);
        }
    }

}
