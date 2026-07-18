# Document and File Metadata Design

`Document` should manage business state only. Physical file details should remain in `FileMetadata`.

Expected `Document` relationship:

```java
@OneToOne(
        fetch = FetchType.LAZY,
        optional = false
)
@JoinColumn(
        name = "file_metadata_id",
        nullable = false,
        unique = true
)
private FileMetadata fileMetadata;
```

Expected table responsibility:

```text
document
- id
- resource_key
- version
- document_status
- index_status
- file_metadata_id
- created_at
- updated_at

file_metadata
- id
- asset_type
- original_file_name
- stored_file_name
- extension
- content_type
- file_size
- storage_relative_path
- access_path
- checksum_sha256
- status
- created_at
- updated_at
```

This keeps document workflow data separate from reusable physical file metadata.
