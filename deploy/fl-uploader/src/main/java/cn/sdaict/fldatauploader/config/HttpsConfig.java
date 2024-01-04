package cn.sdaict.fldatauploader.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: ChangXuan
 * @Decription: 配置同时支持 HTTP 与 HTTPS 访问
 * @Date: 19:39 2020/2/20
 **/
@Configuration
public class HttpsConfig {
    @Value("${server.custom.httpPort}")
    private Integer httpPort;

    @Bean
    public ServletWebServerFactory serverFactory() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        return tomcat;
    }
    /**
     * 配置http
     * @return
     */
    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }
}