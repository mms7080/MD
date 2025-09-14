package com.example.demo.folio.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolioRequestDto {
    private List<String> skills;
    private String introduction;
    private String thumbnail;
}
