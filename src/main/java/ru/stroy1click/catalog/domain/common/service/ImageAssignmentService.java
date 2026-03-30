package ru.stroy1click.catalog.domain.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageAssignmentService<ID> {

    void assignImage(ID id, MultipartFile image);

    void deleteImage(ID id, String link);
}