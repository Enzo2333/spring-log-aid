package zone.huawei.tools.springlogaid.model.requests;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CachedHttpServletRequest extends HttpServletRequestWrapper {

    @Getter
    private byte[] cachedBody;

    private ServletInputStream inputStream;

    @Getter
    private final boolean isMultipart;

    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.isMultipart = StringUtils.startsWithIgnoreCase(request.getContentType(), "multipart/");
        if (this.isMultipart) {
            request.getParameterMap();
        } else {
            ServletInputStream inputStream = request.getInputStream();
            this.cachedBody = StreamUtils.copyToByteArray(inputStream);
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (cachedBody == null) {
            return inputStream;
        }
        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public void reset() {
        if (!this.isMultipart) {
            this.inputStream = new CachedServletInputStream();
            this.cachedBody = null;
        }
    }

    private class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream input;

        public CachedServletInputStream() {
            this.input = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() {
            return input.read();
        }
    }
}
