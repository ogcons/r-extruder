package hr.ogcs.rextruderservice.model;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RScriptModel {
    private int id;
    private byte[] fileData;

}