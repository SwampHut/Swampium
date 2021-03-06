package dev.reactant.reactant.service.spec.file.text

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.Reader

interface TextFileReaderService {
    /**
     * Read all lines in a file into list
     */
    fun readAll(file: File): Single<List<String>>

    fun readAll(fileReader: Reader): Single<List<String>>

    /**
     * Read the file line by line
     */
    fun read(file: File): Flowable<String>

    fun read(fileReader: Reader): Flowable<String>
}
