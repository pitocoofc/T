package com.ndjlib.archiver

import java.io.File

data class FileModel(
    val file: File,
    val isBackOption: Boolean = false
)
