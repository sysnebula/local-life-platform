package com.mishi.platform.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * 分页上限过滤器 — size 最大 100
 */
@Component
public class PageLimitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getParameter("size") != null) {
            try {
                int size = Integer.parseInt(req.getParameter("size"));
                if (size > 100) {
                    chain.doFilter(new Wrapper(req, 100), response);
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }
        chain.doFilter(request, response);
    }

    private static class Wrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params;
        Wrapper(HttpServletRequest req, int max) {
            super(req);
            params = new HashMap<>(req.getParameterMap());
            params.put("size", new String[]{String.valueOf(max)});
        }
        @Override public String getParameter(String n) { return params.containsKey(n) ? params.get(n)[0] : super.getParameter(n); }
        @Override public Map<String, String[]> getParameterMap() { return params; }
        @Override public Enumeration<String> getParameterNames() { return Collections.enumeration(params.keySet()); }
        @Override public String[] getParameterValues(String n) { return params.get(n); }
    }
}
