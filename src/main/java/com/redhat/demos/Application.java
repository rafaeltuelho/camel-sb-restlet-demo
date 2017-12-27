/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.redhat.demos;

import static org.apache.camel.model.rest.RestParamType.path;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.restlet.RestletComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.restlet.ext.spring.SpringServerServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(
            new SpringServerServlet(), "/camel-rest/*");

    	Map<String,String> params = new HashMap<String,String>();
    	params.put("org.restlet.component", "restletComponent");
    	
    	servlet.setInitParameters(params);
    	
        return servlet;
    }

    @Bean
    public org.restlet.Component restletComponent() {
    	return new org.restlet.Component();
    }
    
    @Bean
    public RestletComponent restletComponentService() {
    	return new RestletComponent(restletComponent());
    }    
    
    @Component
    class RestApi extends RouteBuilder {

        @Override
        public void configure() {
            restConfiguration()
                .contextPath("/camel-rest").apiContextPath("/api-doc")
                    .apiProperty("api.title", "Camel REST API")
                    .apiProperty("api.version", "1.0")
                    .apiProperty("cors", "true")
                    .apiProperty("host", "")
                    .apiContextRouteId("doc-api")
                .component("restlet")
                .endpointProperty("socketTimeout", "120000")
                .endpointProperty("connectTimeout", "120000")
                .bindingMode(RestBindingMode.json);

            rest("/hello").description("Just a Hello test endpoint")
                .get("/{tester}").description("say hello")
		        	.param().name("tester").type(path).description("Tester name").dataType("string").endParam()
	                .responseMessage().code(200).message("Success test!").endResponseMessage()
                    .route().routeId("hello-api")
                    .to("direct:hello")
            .endRest();

            rest("/slow").description("Simulates some slow request-reply exchanges")
	            .get("/waitfor/{howlong}").description("The list of all the books")
	        	.param().name("howlong").type(path).description("For how long should my request wait?").dataType("integer").endParam()
                .responseMessage().code(200).message("Success test!").endResponseMessage()
                .responseMessage().code(500).message("Something goes wrong!").endResponseMessage()
                .responseMessage().code(504).message("Request timeout!").endResponseMessage()
	                .route().routeId("slow-api")
	                .to("direct:waitfor")
            .endRest();
        }
    }

    @Component
    class Backend extends RouteBuilder {

        @Override
        public void configure() {
            // A first route to say hello
            from("direct:hello")
                .routeId("hello-route")
                .tracing()
                .transform()
                .simple("Hi ${header.tester}! How are you doing today ( ${date:now} )?");

            // A second route to simulate a slow processing exchange
            from("direct:waitfor")
	            .routeId("slow-route")
	            .tracing()
	            .log("starting at ${date:now}. \nWaiting for ${header.howlong}ms")
	            .delay(simple("${header.howlong}"))
	            .log("ok! finished my sleep time at ${date:now}!")
	            .setBody(constant("ok! finished my sleep time!"));
        }
    }
}