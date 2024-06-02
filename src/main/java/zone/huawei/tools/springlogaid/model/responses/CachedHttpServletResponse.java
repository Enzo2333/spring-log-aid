package zone.huawei.tools.springlogaid.model.responses;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    private final PrintWriter writer = new PrintWriter(byteArrayOutputStream);

    public CachedHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) {
                byteArrayOutputStream.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    public String getBody() throws IOException {
        writer.flush();
        String body = byteArrayOutputStream.toString();
        super.getOutputStream().write(byteArrayOutputStream.toByteArray());
        return body;
    }
}
