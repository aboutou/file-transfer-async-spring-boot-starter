package org.spring.file.transfer.async.core.impl;

import cn.afterturn.easypoi.csv.entity.CsvImportParams;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.annotation.ExcelEntity;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelCollectionParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelImportEntity;
import cn.afterturn.easypoi.excel.entity.result.ExcelVerifyHandlerResult;
import cn.afterturn.easypoi.excel.imports.CellValueService;
import cn.afterturn.easypoi.excel.imports.base.ImportBaseService;
import cn.afterturn.easypoi.exception.excel.ExcelImportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelImportEnum;
import cn.afterturn.easypoi.handler.inter.IExcelDataModel;
import cn.afterturn.easypoi.handler.inter.IExcelDictHandler;
import cn.afterturn.easypoi.handler.inter.IExcelModel;
import cn.afterturn.easypoi.handler.inter.IReadHandler;
import cn.afterturn.easypoi.util.PoiPublicUtil;
import cn.afterturn.easypoi.util.PoiReflectorUtil;
import cn.afterturn.easypoi.util.PoiValidationUtil;
import cn.afterturn.easypoi.util.UnicodeInputStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.utils.ClassUtil;
import org.spring.file.transfer.async.utils.ExcelUtil;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/8 下午2:14
 */
@Slf4j
@NoArgsConstructor
public class EasyPoiFileConverter<T> extends AbstractPoiFileConverter<T> implements ApplicationListener<ApplicationStartedEvent> {

    // private FileFormat defaultFileFormat = FileFormat.EXCEL_07;

    private final static List<FileFormat> fileFormats = Arrays.asList(FileFormat.EXCEL_07, FileFormat.EXCEL_03);

    private final static ExportParams exportParams = new ExportParams();

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        init();
    }

    public void init() {
        if (i18nHandler != null) {
            exportParams.setI18nHandler((name) -> i18nHandler.getLocaleName(name));
        }
    }

    @Override
    public boolean canRead(Class<? extends T> clazz, FileFormat fileFormat) {
        return super.canRead(clazz, fileFormat) || FileFormat.CSV.equals(fileFormat);
    }

    @Override
    public List<T> doRead(InputStream inputStream, MediaType contentType, Class<? extends T> clzss) {
        if (contentType != null && contentType.isCompatibleWith(FileFormat.CSV.getMediaType())) {
            CsvImportParams csvImportParams = new CsvImportParams();
            return new BmCsvImportService(csvImportParams).readExcel(inputStream, clzss, csvImportParams, null);
        } else {
            ImportParams importParams = getImportParams(clzss);
            return (List<T>) ExcelUtil.getExcel(inputStream, clzss, importParams);
        }
    }

    @Override
    public MediaType getContentType(HttpHeaders headers) {
        MediaType contentType = super.getContentType(headers);
        if (contentType != null) {
            return contentType;
        }
        if (contentType != null) {
            if (contentType.isCompatibleWith(FileFormat.CSV.getMediaType())) {
                return contentType;
            }
        }
        String filePath = Optional.ofNullable(headers.getFirst(FILE_PATH)).filter(StringUtils::isNotBlank).orElse(null);
        if (StringUtils.isNotBlank(filePath)) {
            String fileName = FilenameUtils.getExtension(filePath);
            if (StringUtils.endsWithIgnoreCase(fileName, FileFormat.CSV.getFileExtensionName())) {
                return FileFormat.CSV.getMediaType();
            }
        }
        return null;
    }


    @Override
    public List<FileFormat> getFileFormats() {
        return fileFormats;
    }

    @Override
    public Workbook getWorkbook(List<T> data, Class<? extends T> clazz, FileFormat fileFormat) {
        if (CollectionUtils.isEmpty(data)) {
            T r = ClassUtil.newInstance(clazz);
            data = new ArrayList<T>();
            data.add(r);
        }
        List<T> dataSet = new ArrayList<T>(data);
        ExportParams entity = getExportParams(clazz);
        ExcelType excelType = ExcelType.XSSF;
        if (FileFormat.EXCEL_03.equals(fileFormat)) {
            excelType = ExcelType.HSSF;
        }
        entity.setType(excelType);
        T t = dataSet.get(0);
        if (t instanceof IExcelDictHandler) {
            entity.setDictHandler((IExcelDictHandler) t);
        }
        Workbook sheets = ExcelExportUtil.exportExcel(entity, clazz, dataSet);
        return sheets;
    }


    protected ImportParams getImportParams(Class<? extends T> clzss) {
        return ExcelUtil.PARAMS;
    }

    protected ExportParams getExportParams(Class<? extends T> clzss) {
        return exportParams;
    }


    public static class BmCsvImportService extends ImportBaseService {

        private final static Logger LOGGER = LoggerFactory.getLogger(BmCsvImportService.class);

        private final CsvImportParams params;
        private CellValueService cellValueServer;
        private boolean verifyFail = false;

        public BmCsvImportService(CsvImportParams params) {
            this.params = params;
            this.cellValueServer = new CellValueService();
        }

        public <T> List<T> readExcel(InputStream inputstream, Class<?> pojoClass, CsvImportParams params) {
            return readExcel(inputstream, pojoClass, params, null);
        }

        @Override
        public void getAllExcelField(String targetId, Field[] fields, Map<String, ExcelImportEntity> excelParams, List<ExcelCollectionParams> excelCollection, Class<?> pojoClass, List<Method> getMethods, ExcelEntity excelEntityAnn) throws Exception {
            super.getAllExcelField(targetId, fields, excelParams, excelCollection, pojoClass, getMethods, excelEntityAnn);
            Map<String, ExcelImportEntity> excelParams1 = new HashMap<>();
            excelParams.forEach((k, v) -> excelParams1.put(StringUtils.join(params.getTextMark(), k, params.getTextMark()), v));
            excelParams.putAll(excelParams1);
        }


        public <T> List<T> readExcel(InputStream inputstream, Class<?> pojoClass, CsvImportParams params, IReadHandler readHandler) {
            List collection = new ArrayList();
            try {
                Map<String, ExcelImportEntity> excelParams = new HashMap<String, ExcelImportEntity>();
                List<ExcelCollectionParams> excelCollection = new ArrayList<ExcelCollectionParams>();
                String targetId = null;
                i18nHandler = params.getI18nHandler();
                if (!Map.class.equals(pojoClass)) {
                    Field[] fileds = PoiPublicUtil.getClassFields(pojoClass);
                    ExcelTarget etarget = pojoClass.getAnnotation(ExcelTarget.class);
                    if (etarget != null) {
                        targetId = etarget.value();
                    }
                    getAllExcelField(targetId, fileds, excelParams, excelCollection, pojoClass, null, null);
                }

                inputstream = new PushbackInputStream(inputstream, 3);
                byte[] head = new byte[3];
                inputstream.read(head);
                // 判断 UTF8 是不是有 BOM
                if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
                    ((PushbackInputStream) inputstream).unread(head, 0, 3);
                    inputstream = new UnicodeInputStream(inputstream);
                } else {
                    ((PushbackInputStream) inputstream).unread(head, 0, 3);
                }
                BufferedReader rows = new BufferedReader(new InputStreamReader(inputstream, params.getEncoding()));
                int rowIndex = 0;
                for (int j = 0; j < params.getTitleRows(); j++) {
                    rows.readLine();
                    rowIndex++;
                }
                Map<Integer, String> titlemap = getTitleMap(rows, params, excelCollection, excelParams);
                int readRow = 0;
                //跳过无效行
                for (int i = 0; i < params.getStartRows(); i++) {
                    rows.readLine();
                    rowIndex++;
                }
                //判断index 和集合,集合情况默认为第一列
                if (excelCollection.size() > 0 && params.getKeyIndex() == null) {
                    params.setKeyIndex(0);
                }
                StringBuilder errorMsg;
                String row = null;
                Object object = null;
                String[] cells;
                while ((row = rows.readLine()) != null) {
                    rowIndex++;
                    if (StringUtils.isEmpty(row)) {
                        continue;
                    }
                    errorMsg = new StringBuilder();
                    cells = row.split(params.getSpiltMark(), -1);
                    // 判断是集合元素还是不是集合元素,如果是就继续加入这个集合,不是就创建新的对象
                    // keyIndex 如果为空就不处理,仍然处理这一行
                    if (params.getKeyIndex() != null && (cells[params.getKeyIndex()] == null
                            || StringUtils.isEmpty(cells[params.getKeyIndex()]))
                            && object != null) {
                        for (ExcelCollectionParams param : excelCollection) {
                            addListContinue(object, param, row, titlemap, targetId, params, errorMsg);
                        }
                    } else {
                        object = PoiPublicUtil.createObject(pojoClass, targetId);
                        try {
                            Set<Integer> keys = titlemap.keySet();
                            for (Integer cn : keys) {
                                String titleString = titlemap.get(cn);
                                if (excelParams.containsKey(titleString) || Map.class.equals(pojoClass)) {
                                    try {
                                        saveFieldValue(params, object, cells[cn], excelParams, titleString);
                                    } catch (ExcelImportException e) {
                                        // 如果需要去校验就忽略,这个错误,继续执行
                                        if (params.isNeedVerify() && ExcelImportEnum.GET_VALUE_ERROR.equals(e.getType())) {
                                            errorMsg.append(" ").append(titleString).append(ExcelImportEnum.GET_VALUE_ERROR.getMsg());
                                        }
                                    }
                                }
                            }
                            if (object instanceof IExcelDataModel) {
                                ((IExcelDataModel) object).setRowNum(rowIndex);
                            }
                            for (ExcelCollectionParams param : excelCollection) {
                                addListContinue(object, param, row, titlemap, targetId, params, errorMsg);
                            }
                            if (verifyingDataValidity(object, params, pojoClass, errorMsg)) {
                                if (readHandler != null) {
                                    readHandler.handler(object);
                                } else {
                                    collection.add(object);
                                }
                            }
                        } catch (ExcelImportException e) {
                            LOGGER.error("excel import error , row num:{},obj:{}", readRow, ReflectionToStringBuilder.toString(object));
                            if (!e.getType().equals(ExcelImportEnum.VERIFY_ERROR)) {
                                throw new ExcelImportException(e.getType(), e);
                            }
                        } catch (Exception e) {
                            LOGGER.error("excel import error , row num:{},obj:{}", readRow, ReflectionToStringBuilder.toString(object));
                            throw new RuntimeException(e);
                        }
                    }
                    readRow++;
                }
                if (readHandler != null) {
                    readHandler.doAfterAll();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return collection;
        }

        private void addListContinue(Object object, ExcelCollectionParams param, String row,
                                     Map<Integer, String> titlemap, String targetId,
                                     CsvImportParams params, StringBuilder errorMsg) throws Exception {
            Collection collection = (Collection) PoiReflectorUtil.fromCache(object.getClass())
                    .getValue(object, param.getName());
            Object entity = PoiPublicUtil.createObject(param.getType(), targetId);
            // 是否需要加上这个对象
            boolean isUsed = false;
            String[] cells = row.split(params.getSpiltMark());
            for (int i = 0; i < cells.length; i++) {
                String cell = cells[i];
                String titleString = titlemap.get(i);
                if (param.getExcelParams().containsKey(titleString)) {
                    try {
                        saveFieldValue(params, entity, cell, param.getExcelParams(), titleString);
                    } catch (ExcelImportException e) {
                        // 如果需要去校验就忽略,这个错误,继续执行
                        if (params.isNeedVerify() && ExcelImportEnum.GET_VALUE_ERROR.equals(e.getType())) {
                            errorMsg.append(" ").append(titleString).append(ExcelImportEnum.GET_VALUE_ERROR.getMsg());
                        }
                    }
                    isUsed = true;
                }
            }
            if (isUsed) {
                collection.add(entity);
            }
        }

        /**
         * 校验数据合法性
         */
        private boolean verifyingDataValidity(Object object, CsvImportParams params,
                                              Class<?> pojoClass, StringBuilder fieldErrorMsg) {
            boolean isAdd = true;
            Cell cell = null;
            if (params.isNeedVerify()) {
                String errorMsg = PoiValidationUtil.validation(object, params.getVerifyGroup());
                if (StringUtils.isNotEmpty(errorMsg)) {
                    if (object instanceof IExcelModel) {
                        IExcelModel model = (IExcelModel) object;
                        model.setErrorMsg(errorMsg);
                    }
                    isAdd = false;
                    verifyFail = true;
                }
            }
            if (params.getVerifyHandler() != null) {
                ExcelVerifyHandlerResult result = params.getVerifyHandler().verifyHandler(object);
                if (!result.isSuccess()) {
                    if (object instanceof IExcelModel) {
                        IExcelModel model = (IExcelModel) object;
                        model.setErrorMsg((StringUtils.isNoneBlank(model.getErrorMsg())
                                ? model.getErrorMsg() + "," : "") + result.getMsg());
                    }
                    isAdd = false;
                    verifyFail = true;
                }
            }
            if ((params.isNeedVerify() || params.getVerifyHandler() != null) && fieldErrorMsg.length() > 0) {
                if (object instanceof IExcelModel) {
                    IExcelModel model = (IExcelModel) object;
                    model.setErrorMsg((StringUtils.isNoneBlank(model.getErrorMsg())
                            ? model.getErrorMsg() + "," : "") + fieldErrorMsg.toString());
                }
                isAdd = false;
                verifyFail = true;
            }
            return isAdd;
        }

        /**
         * 保存字段值(获取值,校验值,追加错误信息)
         */
        private void saveFieldValue(CsvImportParams params, Object object, String cell,
                                    Map<String, ExcelImportEntity> excelParams, String titleString) throws Exception {
            if (cell.startsWith(params.getTextMark()) && cell.endsWith(params.getTextMark())) {
                //FIXED 字符串截取时考虑的情况不全面导致的BUG
                cell = cell.substring(params.getTextMark().length(), cell.lastIndexOf(params.getTextMark()));
            }
            Object value = cellValueServer.getValue(params.getDataHandler(), object, cell, excelParams,
                    titleString, params.getDictHandler());
            if (object instanceof Map) {
                if (params.getDataHandler() != null) {
                    params.getDataHandler().setMapValue((Map) object, titleString, value);
                } else {
                    ((Map) object).put(titleString, value);
                }
            } else {
                setValues(excelParams.get(titleString), object, value);
            }
        }

        /**
         * 获取表格字段列名对应信息
         */
        private Map<Integer, String> getTitleMap(BufferedReader rows, CsvImportParams params,
                                                 List<ExcelCollectionParams> excelCollection,
                                                 Map<String, ExcelImportEntity> excelParams) throws IOException {
            Map<Integer, String> titlemap = new LinkedHashMap<Integer, String>();
            String collectionName = null;
            ExcelCollectionParams collectionParams = null;
            String row = null;
            String[] cellTitle;
            for (int j = 0; j < params.getHeadRows(); j++) {
                row = rows.readLine();
                if (row == null) {
                    continue;
                }
                cellTitle = row.split(params.getSpiltMark());
                for (int i = 0; i < cellTitle.length; i++) {
                    String value = cellTitle[i];
                    //用以支持重名导入
                    if (StringUtils.isNotEmpty(value)) {
                        if (titlemap.containsKey(i)) {
                            collectionName = titlemap.get(i);
                            collectionParams = getCollectionParams(excelCollection, collectionName);
                            titlemap.put(i, collectionName + "_" + value);
                        } else if (StringUtils.isNotEmpty(collectionName) && collectionParams != null
                                && collectionParams.getExcelParams()
                                .containsKey(collectionName + "_" + value)) {
                            titlemap.put(i, collectionName + "_" + value);
                        } else {
                            collectionName = null;
                            collectionParams = null;
                        }
                        if (StringUtils.isEmpty(collectionName)) {
                            titlemap.put(i, value);
                        }
                    }
                }
            }

            // 处理指定列的情况
            Set<String> keys = excelParams.keySet();
            for (String key : keys) {
                if (key.startsWith("FIXED_")) {
                    String[] arr = key.split("_");
                    titlemap.put(Integer.parseInt(arr[1]), key);
                }
            }
            return titlemap;
        }

        /**
         * 获取这个名称对应的集合信息
         */
        private ExcelCollectionParams getCollectionParams(List<ExcelCollectionParams> excelCollection,
                                                          String collectionName) {
            for (ExcelCollectionParams excelCollectionParams : excelCollection) {
                if (collectionName.equals(excelCollectionParams.getExcelName())) {
                    return excelCollectionParams;
                }
            }
            return null;
        }
    }
}
