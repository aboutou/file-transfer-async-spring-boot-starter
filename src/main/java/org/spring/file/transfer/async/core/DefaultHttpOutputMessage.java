package org.spring.file.transfer.async.core;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author tiny
 * 
 * @since 2023/5/14 上午10:27
 */
public class DefaultHttpOutputMessage implements HttpOutputMessage {
    private OutputStream outputStream;
    private HttpHeaders httpHeaders;

    public DefaultHttpOutputMessage(OutputStream outputStream) {
        this(outputStream, new HttpHeaders());
    }

    public DefaultHttpOutputMessage(OutputStream outputStream, HttpHeaders httpHeaders) {
        this.outputStream = outputStream;
        this.httpHeaders = httpHeaders;
    }

    @Override
    public OutputStream getBody() throws IOException {
        return outputStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

}
