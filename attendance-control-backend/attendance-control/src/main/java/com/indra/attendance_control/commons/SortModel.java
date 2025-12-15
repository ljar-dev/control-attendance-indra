package com.indra.attendance_control.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortModel {
    private String colName;
    private String direction; // "ASC" o "DESC"
}