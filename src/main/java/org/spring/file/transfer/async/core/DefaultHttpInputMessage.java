package org.spring.file.transfer.async.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author tiny
 * 
 * @since 2023/5/14 上午10:25
 */
public class DefaultHttpInputMessage implements HttpInputMessage {

    private ByteArrayOutputStream out;
    private HttpHeaders httpHeaders;

    public DefaultHttpInputMessage(InputStream inputStream) {
        this(inputStream, new HttpHeaders());
    }

    public DefaultHttpInputMessage(InputStream inputStream, HttpHeaders httpHeaders) {
        if (inputStream instanceof ByteArrayInputStream) {
            try {
                inputStream.reset();
            } catch (IOException e) {
            }
        }
        try {
            out = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, out);
            out.flush();
            // bufferedOutputStream = new BufferedOutputStream(out);
        } catch (IOException e) {

        }
        this.httpHeaders = httpHeaders;
    }

    public void setContentType(String contentType) {
        setContentType(MediaType.parseMediaType(contentType));
    }

    public void setContentType(MediaType contentType) {
        if (ObjectUtils.allNotNull(contentType, httpHeaders)) {
            httpHeaders.setContentType(contentType);
        }
    }

    public MediaType getContentType() {
        if (Objects.nonNull(httpHeaders)) {
            return httpHeaders.getContentType();
        }
        return null;
    }

    @Override
    public InputStream getBody() {
        return new AutoCloseInputStream(out.toInputStream());
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }
}
