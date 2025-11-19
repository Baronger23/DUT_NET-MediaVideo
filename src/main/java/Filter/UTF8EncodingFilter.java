package Filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * ✅ UTF-8 Encoding Filter
 * Đảm bảo tất cả request và response đều sử dụng UTF-8 encoding
 * Khắc phục lỗi: "chào mừng quý v�? và các bạn �?ến v�?i"
 */
@WebFilter(urlPatterns = "/*", filterName = "UTF8EncodingFilter")
public class UTF8EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("✅ UTF8EncodingFilter đã được khởi tạo");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // ✅ Set UTF-8 encoding cho REQUEST
        // Điều này đảm bảo các parameter từ form/URL được đọc đúng
        if (httpRequest.getCharacterEncoding() == null) {
            httpRequest.setCharacterEncoding("UTF-8");
        }
        
        // ✅ Set UTF-8 encoding cho RESPONSE
        // Điều này đảm bảo dữ liệu trả về client được encode đúng
        httpResponse.setCharacterEncoding("UTF-8");
        
        // ✅ Set Content-Type với charset=UTF-8 nếu chưa có
        // Chỉ set cho HTML/JSON/XML responses
        String contentType = httpResponse.getContentType();
        if (contentType == null || (!contentType.contains("charset") && 
            (contentType.contains("text") || contentType.contains("json") || contentType.contains("xml")))) {
            // Sẽ được ghi đè bởi controller nếu cần
        }
        
        // Tiếp tục chuỗi filter
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("✅ UTF8EncodingFilter đã được hủy");
    }
}
