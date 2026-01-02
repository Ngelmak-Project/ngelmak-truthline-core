package org.ngelmakproject.web.rest.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

  private List<T> content; // The actual page content (list of elements)
  private int number; // Current page number
  private int size; // Number of elements per page
  private long totalElements; // Total number of elements across all pages
  private int totalPages; // Total number of pages
  private boolean isLast; // Is this the last page
  private boolean isFirst; // Is this the first page
  private boolean hasNext; // Is there a next page
  private boolean hasPrevious; // Is there a previous page
  private List<SortDTO> sorts; // Sorting information

  public PageDTO(Page<T> page) {
    content = page.getContent();
    number = page.getNumber();
    size = page.getSize();
    totalElements = page.getTotalElements();
    totalPages = page.getTotalPages();
    isFirst = page.isFirst();
    isLast = page.isLast();
    hasNext = page.hasNext();
    hasPrevious = page.hasPrevious();
    sorts = page.getSort().stream()
        .map(order -> new SortDTO(order.getProperty(), order.getDirection().name()))
        .collect(Collectors.toList());
  }
}
