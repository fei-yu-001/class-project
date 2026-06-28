package com.salary.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * XSS 防护过滤器
 *
 * - JSON 请求体: 使用 Jackson 解析后对所有字符串值做 HTML 转义，再重新序列化
 * - 查询参数: 对 parameter 值做 XSS 消毒
 * - 跳过 multipart/form-data 和非文本请求（防止破坏文件上传）
 * - 不修改 Header（Authorization 等 header 被编码会破坏认证）
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class XssFilter implements Filter {

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 只处理 JSON 请求体，跳过 multipart 和其他非文本类型
        String contentType = httpRequest.getContentType();
        boolean isJson = contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);

        if (isJson) {
            chain.doFilter(new JsonSanitizingWrapper(httpRequest, objectMapper), response);
        } else {
            chain.doFilter(new ParameterSanitizingWrapper(httpRequest), response);
        }
    }

    /**
     * 仅对查询参数做 XSS 消毒（不影响 Header 和 body）
     */
    private static class ParameterSanitizingWrapper extends HttpServletRequestWrapper {
        public ParameterSanitizingWrapper(HttpServletRequest request) { super(request); }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? sanitize(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] result = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = sanitize(values[i]);
            }
            return result;
        }

        private String sanitize(String value) {
            if (value == null) return null;
            return value
                .replaceAll("<script[\\s>]", "&lt;script ")
                .replaceAll("</script>", "&lt;/script&gt;")
                .replaceAll("javascript:", "javascript&#58;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        }
    }

    /**
     * 使用 Jackson 解析 JSON，对所有字符串值做 XSS 消毒后重新序列化
     * 不改变 JSON 结构，不会破坏嵌套对象或数组
     */
    private static class JsonSanitizingWrapper extends HttpServletRequestWrapper {

        private byte[] cachedBody;
        private byte[] sanitizedBody;
        private final ObjectMapper mapper;

        public JsonSanitizingWrapper(HttpServletRequest request, ObjectMapper mapper) throws IOException {
            super(request);
            this.mapper = mapper;
            InputStream is = request.getInputStream();
            this.cachedBody = is.readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (sanitizedBody == null) {
                String original = new String(cachedBody, StandardCharsets.UTF_8);
                try {
                    JsonNode tree = mapper.readTree(original);
                    sanitizeNode(tree);
                    sanitizedBody = mapper.writeValueAsString(tree).getBytes(StandardCharsets.UTF_8);
                } catch (Exception e) {
                    sanitizedBody = cachedBody;
                }
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(sanitizedBody);
            return new DelegatingServletInputStream(bais);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        private void sanitizeNode(JsonNode node) {
            if (node.isObject()) {
                ObjectNode obj = (ObjectNode) node;
                obj.fields().forEachRemaining(entry -> {
                    JsonNode child = entry.getValue();
                    if (child.isTextual()) {
                        entry.setValue(new TextNode(sanitizeHtml(child.asText())));
                    } else {
                        sanitizeNode(child);
                    }
                });
            } else if (node.isArray()) {
                ArrayNode arr = (ArrayNode) node;
                for (int i = 0; i < arr.size(); i++) {
                    JsonNode child = arr.get(i);
                    if (child.isTextual()) {
                        arr.set(i, new TextNode(sanitizeHtml(child.asText())));
                    } else {
                        sanitizeNode(child);
                    }
                }
            }
        }

        private String sanitizeHtml(String value) {
            if (value == null) return null;
            return value
                .replaceAll("<script[\\s>]", "&lt;script ")
                .replaceAll("</script>", "&lt;/script&gt;")
                .replaceAll("javascript:", "javascript&#58;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        }
    }

    /**
     * 包装 ByteArrayInputStream 为 ServletInputStream
     */
    private static class DelegatingServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream source;
        DelegatingServletInputStream(ByteArrayInputStream source) { this.source = source; }
        @Override public boolean isFinished() { return source.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener listener) {}
        @Override public int read() { return source.read(); }
    }
}
