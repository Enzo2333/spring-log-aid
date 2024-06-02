package zone.huawei.tools.springlogaid.model.requests;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CachedHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        ServletInputStream inputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(inputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new CachedServletInputStream()));
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
