package com.example.halo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHuntSyncResultDto {
    private String mode;
    private int pagesProcessed;
    private int upserted;
    private boolean hasNextPage;
    private String endCursor;
    private int retries;
}
