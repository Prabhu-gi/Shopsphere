package com.shopsphere.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String subdirectory);

    void delete(String fileUrl);
}

