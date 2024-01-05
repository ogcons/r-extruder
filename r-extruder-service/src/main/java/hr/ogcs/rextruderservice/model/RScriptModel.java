package hr.ogcs.rextruderservice.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RScriptModel {
    private int id;
    private byte[] fileData;

}