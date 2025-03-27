package org.spring.file.transfer.async.core.impl;

import org.spring.file.transfer.async.commons.FileContentType;
import org.spring.file.transfer.async.core.DefaultHttpInputMessage;
import org.spring.file.transfer.async.core.FileContentConverter;
import org.spring.file.transfer.async.core.exception.TaskException;
import org.spring.file.transfer.async.core.export.service.TaskFileExportService;
import org.spring.file.transfer.async.core.imports.model.FileContentModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

/**
 * @author wuzhencheng
 */
@Slf4j
public class HttpMultipartFormDataFileContentConverterImpl implements FileContentConverter {

    @Override
    public boolean supports(FileContentType fileContentType) {
        MediaType contentType = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).map(ServletRequestAttributes::getRequest).map(p -> p.getContentType()).map(MediaType::parseMediaType).orElse(null);
        return MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)
                || FileContentType.MULTIPART_FORM_DATA.equals(fileContentType);
    }

    @Override
    public HttpInputMessage remoteConvert(FileContentModel fileContent) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        HttpServletRequest request = requestAttributes.getRequest();
        if (request == null) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        if (!(request instanceof MultipartHttpServletRequest)) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        /*if (!ServletFileUpload.isMultipartContent(request)) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        log.info("{} aaaa {}", request, request.getClass());

        MultipartHttpServletRequest multipartRequest = new CommonsMultipartResolver().resolveMultipart(getNativeRequest(request));
        //获取multiRequest 中所有的文件名
        Iterator<String> iter = multipartRequest.getFileNames();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }*/
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator<String> fileNames = multipartRequest.getFileNames();
        if (IteratorUtils.isEmpty(fileNames)) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件，请检查是否上传了文件");
        }
        //MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
        if (multipartRequest == null) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        MultipartFile file = multipartRequest.getFile("file");
        if (file == null) {
            throw new TaskException("无法从http[" + MediaType.MULTIPART_FORM_DATA_VALUE + "]解析到文件");
        }
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
        DefaultHttpInputMessage httpInputMessage = new DefaultHttpInputMessage(inputStream);
        httpInputMessage.setContentType(file.getContentType());
        httpInputMessage.getHeaders().set(TaskFileExportService.FILE_NAME_HEADER, file.getOriginalFilename());
        return httpInputMessage;
    }

    private HttpServletRequest getNativeRequest(HttpServletRequest request) {
        if (request instanceof ServletRequestWrapper) {
            return getNativeRequest((HttpServletRequest) ((ServletRequestWrapper) request).getRequest());
        }
        return request;
    }
}
