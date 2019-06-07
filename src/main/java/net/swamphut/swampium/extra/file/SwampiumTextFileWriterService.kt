package net.swamphut.swampium.extra.file

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import net.swamphut.swampium.core.swobject.container.SwObject
import net.swamphut.swampium.service.spec.file.text.TextFileWriterService

import java.io.File
import java.io.FileWriter

@SwObject
class SwampiumTextFileWriterService : TextFileWriterService {
    override fun append(file: File, string: String): Completable {
        return write(file, string, true)
    }

    override fun write(file: File, string: String): Completable {
        return write(file, string, false)
    }

    protected fun write(file: File, string: String, append: Boolean): Completable {
        return Completable.fromAction {
            if (file.exists() && !file.isFile) throw IllegalArgumentException(file.name + " is not a file")
            file.parentFile.mkdirs()
            file.createNewFile()

            val writer = FileWriter(file, append)
            writer.write(string)
            writer.flush()

            writer.close()
        }.subscribeOn(Schedulers.io())
    }
}
