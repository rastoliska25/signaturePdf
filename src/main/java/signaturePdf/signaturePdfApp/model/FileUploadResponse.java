package signaturePdf.signaturePdfApp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FileUploadResponse {

    private String fileName;

    private long size;

}