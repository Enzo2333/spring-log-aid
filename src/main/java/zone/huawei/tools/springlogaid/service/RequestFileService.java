package zone.huawei.tools.springlogaid.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface RequestFileService {

    String handleRequestFileAndRecord(HttpServletRequest request, MultipartFile file);
}
