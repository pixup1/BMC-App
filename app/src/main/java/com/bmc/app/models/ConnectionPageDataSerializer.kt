package com.bmc.app.models

import com.bmc.app.models.ConnectionPageData
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ConnectionPageDataSerializer : Serializer<ConnectionPageData> {
    override val defaultValue: ConnectionPageData = ConnectionPageData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ConnectionPageData {
        try {
            return ConnectionPageData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ConnectionPageData,
        output: OutputStream
    ) {
        t.writeTo(output)
    }
}