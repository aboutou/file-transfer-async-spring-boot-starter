package org.spring.file.transfer.async.web.dto.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.entities.model.Res;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author bm
 */
@Getter
@Setter
@ToString
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder(toBuilder = true)
public class AsyncTaskResultOutput extends Res {

    private Duration executeDuration;

    /**
     * 业务类型描述
     */
    private String taskTypeDesc;

    private String projectName;

    /**
     * 任务状态
     */
    private String taskStateDesc;

    /**
     * 预计完成时长
     */
    private Long planFinishTime;

    /**
     * 失败的数据
     */
    @JsonProperty("failResults")
    private List<Map<String, String>> failResultMaps;


    @Override
    @JsonIgnore
    public List<TaskFailResultInstance> getFailResults() {
        return super.getFailResults();
    }

    public AsyncTaskResultOutput() {

    }

    public AsyncTaskResultOutput(Res res) {

    }

   @Mapper
    public interface AsyncTaskResultMapper {


        AsyncTaskResultMapper INSTANCE = Mappers.getMapper(AsyncTaskResultMapper.class);


    }
}
