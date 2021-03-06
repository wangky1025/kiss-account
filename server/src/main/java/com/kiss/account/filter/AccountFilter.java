package com.kiss.account.filter;

import com.kiss.account.utils.LangUtil;
import filter.GuestFilter;
import filter.InnerFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import utils.ThreadLocalUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(filterName = "responseFilter", urlPatterns = "/*")
public class AccountFilter implements Filter {

    private LangUtil langUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        langUtil = context.getBean(LangUtil.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        ResponseWrapper responseWrapper = new ResponseWrapper(httpServletResponse);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        InnerFilterChain preFilterChain = new InnerFilterChain();

        if (!httpServletRequest.getRequestURI().contains("/login") && !httpServletRequest.getRequestURI().contains("/client/authorization") && !httpServletRequest.getRequestURI().contains("/root/check") && !httpServletRequest.getRequestURI().contains("/root")) {
            GuestFilter userInfoFilter = new GuestFilter();
            preFilterChain.addFilter(userInfoFilter);
        }

        preFilterChain.doFilter(httpServletRequest, httpServletResponse, preFilterChain);
        chain.doFilter(httpServletRequest, responseWrapper);
        InnerFilterChain suffixFilterChain = new InnerFilterChain();
        suffixFilterChain.addFilter(new ResponseFilter(responseWrapper, langUtil));
        suffixFilterChain.doFilter(httpServletRequest, httpServletResponse, suffixFilterChain);
        ThreadLocalUtil.remove();
    }

    @Override
    public void destroy() {

    }
}
