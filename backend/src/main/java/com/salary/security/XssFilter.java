package com.salary.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }

    private static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private byte[] cachedBody;

        public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            InputStream inputStream = request.getInputStream();
            this.cachedBody = inputStream.readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            String body = new String(cachedBody, "UTF-8");
            String sanitized = sanitizeJsonValues(body);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sanitized.getBytes("UTF-8"));
            return new DelegatingServletInputStream(byteArrayInputStream);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String body = new String(cachedBody, "UTF-8");
            String sanitized = sanitizeJsonValues(body);
            return new BufferedReader(new StringReader(sanitized));
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? sanitize(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return value != null ? sanitize(value) : null;
        }

        /**
         * Sanitize JSON string values while preserving JSON structure.
         * Walks through the JSON character by character, only sanitizing
         * content inside quoted string values.
         */
        private String sanitizeJsonValues(String json) {
            if (json == null || json.isEmpty()) return json;

            StringBuilder result = new StringBuilder(json.length());
            boolean inString = false;
            boolean escape = false;
            // Track the last non-whitespace character outside strings
            char lastSignificant = 0;

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);

                if (escape) {
                    result.append(c);
                    escape = false;
                    continue;
                }

                if (c == '\\' && inString) {
                    result.append(c);
                    escape = true;
                    continue;
                }

                if (c == '"') {
                    if (inString) {
                        // End of string value
                        inString = false;
                        result.append(c);
                    } else {
                        // A quote starts a value if preceded by : or [
                        if (lastSignificant == ':' || lastSignificant == '[') {
                            inString = true;
                        }
                        result.append(c);
                        lastSignificant = c;
                    }
                    continue;
                }

                if (!inString && !Character.isWhitespace(c)) {
                    lastSignificant = c;
                }

                if (inString) {
                    result.append(sanitizeChar(c));
                } else {
                    result.append(c);
                }
            }

            return result.toString();
        }

        private char[] sanitizeChar(char c) {
            switch (c) {
                case '<': return "&lt;".toCharArray();
                case '>': return "&gt;".toCharArray();
                default: return new char[]{c};
            }
        }

        private String sanitize(String value) {
            if (value == null) return null;
            return value
                .replaceAll("<script", "&lt;script")
                .replaceAll("</script>", "&lt;/script&gt;")
                .replaceAll("javascript:", "javascript&#58;")
                .replaceAll("on\\w+\\s*=", "blocked_event=")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;");
        }

        /**
         * Wraps a ByteArrayInputStream as a ServletInputStream.
         */
        private static class DelegatingServletInputStream extends ServletInputStream {

            private final ByteArrayInputStream sourceStream;

            public DelegatingServletInputStream(ByteArrayInputStream sourceStream) {
                this.sourceStream = sourceStream;
            }

            @Override
            public boolean isFinished() {
                return sourceStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return sourceStream.read();
            }
        }
    }
}
