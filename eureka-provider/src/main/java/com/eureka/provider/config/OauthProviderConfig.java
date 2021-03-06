package com.eureka.provider.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableResourceServer
public class OauthProviderConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    /**
     * 指定token的持久化策略
     *
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * 指定当前资源存储的id和存储方案
     *
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("provider_api").tokenStore(tokenStore());
    }

    /**
     * 指定不同请求方式访问资源所需要的权限(固定配置)
     *
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('read')")
            .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('write')")
            .antMatchers(HttpMethod.PATCH, "/**").access("#oauth2.hasScope('write')")
            .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('write')")
            .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('write')")
            .and()
            .headers().addHeaderWriter((request, response) -> {
                //允许跨域
                response.addHeader("Access-Control-Allow-Orign", "*");
                //如果是跨域请求的预检请求，则向下传递请求头
                if (request.getMethod().equals("OPTIONS")) {
                    response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Allow-Methods"));
                    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Allow-Headers"));
                }

            });
    }

}
