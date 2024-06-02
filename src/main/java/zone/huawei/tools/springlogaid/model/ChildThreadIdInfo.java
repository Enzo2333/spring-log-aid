package zone.huawei.tools.springlogaid.model;

import lombok.Data;

import java.util.List;

@Data
public class ChildThreadIdInfo {

    private List<String> childThreadLogIds;

    private List<String> childThreadErrorLogIds;
}
