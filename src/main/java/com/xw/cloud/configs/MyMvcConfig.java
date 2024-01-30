package com.xw.cloud.configs;


import com.xw.cloud.interceptor.LoginInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.nio.charset.Charset;
import java.util.List;


@Configuration
@EnableWebMvc
public class MyMvcConfig implements WebMvcConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Bean
  public ResourceBundleMessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setBasename("i18n.messages");
    messageSource.setCacheSeconds(60);
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }

  @Bean(name = "beanNameViewResolver")
  public BeanNameViewResolver beanNameViewResolver() {
    BeanNameViewResolver resolver = new BeanNameViewResolver();
    resolver.setOrder(0);
    return resolver;
  }


  @Bean(name = "jsonView")
  public MappingJackson2JsonView mappingJackson2JsonView() {
    MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
    mappingJackson2JsonView.setContentType("application/json;charset=UTF-8");
    return mappingJackson2JsonView;
  }

  @Bean
  public HttpMessageConverter<String> httpMessageConverter() {
    return new StringHttpMessageConverter(Charset.forName("UTF-8"));
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {

  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {

  }

  @Override
  public void addFormatters(FormatterRegistry registry) {

  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoginInterceptor()).
            addPathPatterns("/**").
            excludePathPatterns("/", "/main").
            excludePathPatterns("/css/*", "/js/*", "/img/*");
  }




  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/").setCachePeriod(31556926);
    registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/assets/css/").setCachePeriod(31556926);
    registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/assets/js/").setCachePeriod(31556926);
    registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/assets/images/").setCachePeriod(31556926);
    registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/assets/fonts/").setCachePeriod(31556926);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {

  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {

  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {

  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

  }

  @Override
  public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {

  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

  }

  @Override
  public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {

  }

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {

  }

  @Override
  public Validator getValidator() {
    return null;
  }

  @Override
  public MessageCodesResolver getMessageCodesResolver() {
    return null;
  }
}
