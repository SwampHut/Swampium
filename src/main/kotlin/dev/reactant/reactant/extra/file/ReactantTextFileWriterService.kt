package dev.reactant.reactant.extra.file

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.dependency.layers.SystemLevel
import dev.reactant.reactant.service.spec.file.text.TextFileWriterService
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileWriter

@Component
class ReactantTextFileWriterService : TextFileWriterService, SystemLevel {
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
