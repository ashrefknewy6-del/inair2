package com.example.file

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.model.FileTransferItem
import com.example.model.TransferStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class FileShareManager(private val context: Context) {

    private val _sharedFolderUri = MutableStateFlow<Uri?>(null)
    val sharedFolderUri: StateFlow<Uri?> = _sharedFolderUri.asStateFlow()

    private val _sharedFolderName = MutableStateFlow<String>("InAir Shared Storage")
    val sharedFolderName: StateFlow<String> = _sharedFolderName.asStateFlow()

    private val _transferQueue = MutableStateFlow<List<FileTransferItem>>(emptyList())
    val transferQueue: StateFlow<List<FileTransferItem>> = _transferQueue.asStateFlow()

    private val defaultSharedDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "InAir_Shared")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun setSharedFolderUri(uri: Uri?) {
        _sharedFolderUri.value = uri
        if (uri != null) {
            val doc = DocumentFile.fromTreeUri(context, uri)
            _sharedFolderName.value = doc?.name ?: "Custom Folder"
        } else {
            _sharedFolderName.value = "InAir Shared Storage"
        }
    }

    data class RemoteFileSummary(
        val name: String,
        val size: Long,
        val isDirectory: Boolean,
        val lastModified: Long
    )

    fun listSharedFiles(): List<RemoteFileSummary> {
        val uri = _sharedFolderUri.value
        if (uri != null) {
            val tree = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
            return tree.listFiles().mapNotNull { doc ->
                val name = doc.name ?: return@mapNotNull null
                RemoteFileSummary(
                    name = name,
                    size = doc.length(),
                    isDirectory = doc.isDirectory,
                    lastModified = doc.lastModified()
                )
            }
        } else {
            return defaultSharedDir.listFiles()?.map { file ->
                RemoteFileSummary(
                    name = file.name,
                    size = file.length(),
                    isDirectory = file.isDirectory,
                    lastModified = file.lastModified()
                )
            } ?: emptyList()
        }
    }

    fun openFileInputStream(fileName: String): InputStream? {
        val uri = _sharedFolderUri.value
        if (uri != null) {
            val tree = DocumentFile.fromTreeUri(context, uri) ?: return null
            val fileDoc = tree.findFile(fileName) ?: return null
            return context.contentResolver.openInputStream(fileDoc.uri)
        } else {
            val target = File(defaultSharedDir, fileName)
            if (!target.exists() || !target.isFile) return null
            return target.inputStream()
        }
    }

    fun saveIncomingFile(fileName: String, inputStream: InputStream, totalSize: Long): Boolean {
        val transferId = UUID.randomUUID().toString()
        val item = FileTransferItem(
            id = transferId,
            fileName = fileName,
            totalBytes = totalSize,
            bytesTransferred = 0,
            isUpload = true,
            status = TransferStatus.TRANSFERRING
        )
        updateTransferItem(item)

        var outputStream: OutputStream? = null
        try {
            val uri = _sharedFolderUri.value
            outputStream = if (uri != null) {
                val tree = DocumentFile.fromTreeUri(context, uri)
                val existing = tree?.findFile(fileName)
                existing?.delete()
                val newFile = tree?.createFile("application/octet-stream", fileName)
                if (newFile != null) {
                    context.contentResolver.openOutputStream(newFile.uri)
                } else {
                    null
                }
            } else {
                val target = File(defaultSharedDir, fileName)
                if (target.exists()) target.delete()
                target.outputStream() as OutputStream
            }

            if (outputStream == null) {
                updateTransferItem(item.copy(status = TransferStatus.FAILED))
                return false
            }

            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var totalWritten = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalWritten += bytesRead
                updateTransferItem(
                    item.copy(
                        bytesTransferred = totalWritten,
                        status = TransferStatus.TRANSFERRING
                    )
                )
            }
            outputStream.flush()
            updateTransferItem(
                item.copy(
                    bytesTransferred = totalWritten,
                    status = TransferStatus.COMPLETED
                )
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            updateTransferItem(item.copy(status = TransferStatus.FAILED))
            return false
        } finally {
            outputStream?.close()
        }
    }

    private fun updateTransferItem(item: FileTransferItem) {
        val current = _transferQueue.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(0, item)
        }
        _transferQueue.value = current.take(20) // Keep latest 20
    }
}
